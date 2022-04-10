package main

import (
	"bufio"
	"bytes"
	"encoding/csv"
	"fmt"
	"io"
	"os"
	"regexp"
	"strings"

	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/aws/session"
	"github.com/aws/aws-sdk-go/service/s3"
	"github.com/aws/aws-sdk-go/service/s3/s3manager"
	dem "github.com/markus-wa/demoinfocs-golang/v2/pkg/demoinfocs"
)

func createPlayerMapping(p dem.Parser) map[uint64]string {
	playerMapping := make(map[uint64]string)
	for i, player := range p.GameState().TeamTerrorists().Members() {
		playerMapping[player.SteamID64] = fmt.Sprintf("team0_player%d", i)
	}
	for i, player := range p.GameState().TeamCounterTerrorists().Members() {
		playerMapping[player.SteamID64] = fmt.Sprintf("team1_player%d", i)
	}
	return playerMapping
}

func createTeamMapping(p dem.Parser) map[string]string {
	teamMapping := make(map[string]string)
	teamMapping[p.GameState().TeamTerrorists().ClanName()] = "team0"
	teamMapping[p.GameState().TeamCounterTerrorists().ClanName()] = "team1"
	return teamMapping
}

func main() {
	sess, _ := session.NewSession(&aws.Config{
		Region: aws.String("eu-west-1"),
	})

	svc := s3.New(sess)

	regexPattern := `demo(\d+)\/(.*)\.dem`
	downloader := s3manager.NewDownloader(sess, func(d *s3manager.Downloader) { d.PartSize = 100 * 1024 * 1024 })
	uploader := s3manager.NewUploader(sess, func(d *s3manager.Uploader) { d.PartSize = 100 * 1024 * 1024 })

	objKey := os.Args[1]
	r, _ := regexp.Compile(regexPattern)
	submatches := r.FindStringSubmatch(objKey)
	id := submatches[1]
	suffix := submatches[2]
	_, err := svc.GetObject(&s3.GetObjectInput{
		Bucket: aws.String("csgo-parsed-demos-rushb"),
		Key:    aws.String(id + "_" + suffix + ".csv"),
	})

	if err != nil && strings.HasPrefix(err.Error(), s3.ErrCodeNoSuchKey) {
		println("Downloading " + objKey)
		head, _ := downloader.S3.HeadObject(&s3.HeadObjectInput{
			Bucket: aws.String("csgo-unpacked-demos"),
			Key:    aws.String(objKey),
		})
		buffer := aws.NewWriteAtBuffer(make([]byte, 0, *head.ContentLength))
		downloader.Download(buffer, &s3.GetObjectInput{
			Bucket: aws.String("csgo-unpacked-demos"),
			Key:    aws.String(objKey),
		})

		println("Downloading finished. Parsing")
		reader := bytes.NewReader(buffer.Bytes())
		resBuffer, _ := parse(reader, id)

		println("Parsing finished. Uploading")

		uploader.Upload(&s3manager.UploadInput{
			Bucket:      aws.String("csgo-parsed-demos-rushb"),
			Key:         aws.String(id + "_" + suffix + ".csv"),
			Body:        &resBuffer,
			ContentType: aws.String("application/csv"),
		})

		resBuffer.Reset()
		println("Finished " + objKey)
	} else {
		println("Skipping " + objKey + " because it already exists")
	}
}

func remapTeams[V any](input map[string]V, mappings map[string]string) map[string]V {
	output := make(map[string]V)
	for id, entry := range input {
		output[mappings[id]] = entry
	}

	return output
}

func remapPlayers[V any](input map[uint64]V, mappings map[uint64]string) map[string]V {
	output := make(map[string]V)
	for id, entry := range input {
		output[mappings[id]] = entry
	}
	return output
}

func parse(stream io.Reader, id string) (bytes.Buffer, error) {
	var buffer bytes.Buffer
	p := dem.NewParser(stream)
	defer p.Close()

	_, err := p.ParseHeader()

	if err != nil {
		return buffer, err
	}

	for p.GameState().IsWarmupPeriod() || !p.GameState().IsMatchStarted() {
		p.ParseNextFrame()
	}

	playerMapping := createPlayerMapping(p)
	teamMapping := createTeamMapping(p)

	names := map[string]map[string]string{
		"name": {
			"team0": p.GameState().TeamTerrorists().ClanName(),
			"team1": p.GameState().TeamCounterTerrorists().ClanName(),
		},
	}

	var fs FlashStatistics
	var ef EntryFragsStatistics
	var dw DuelsWonStatistics
	var re RefragStatistics
	var cw ClutchesWonStatistics
	var gd GrenadeDamage
	var sm SavedMoney

	var mo MapOccupation
	var rw RoundsWonStatistics
	var gs GoalStatistics

	register := func(p dem.Parser) {
		fs = createFlashStatistics()
		ef = createEntryFragsStatistics()
		dw = createDuelsWonStatistics()
		re = createRefragStatistics()
		cw = createClutchesWonStatistics()
		gd = createGrenadeDamage()
		sm = createSavedMoney()

		mo = createMapOccupation()
		rw = createRoundsWon()
		gs = createGoalStatistics()

		fs.register(p)
		ef.register(p)
		dw.register(p)
		mo.register(p)
		re.register(p)
		rw.register(p)
		gs.register(p)
		cw.register(p)
		gd.register(p)
		sm.register(p)
	}

	// p.RegisterEventHandler(func(e events.RoundStart) {
	// 	if p.GameState().TotalRoundsPlayed() == 0 {
	// 		register(p)
	// 	}
	// })

	register(p)

	// Parse to end
	err = p.ParseToEnd()

	if err != nil {
		return buffer, err
	}

	fsr := remapPlayers(fs.flashMap, playerMapping)
	far := remapPlayers(fs.flashAssist, playerMapping)
	efr := remapPlayers(ef.efMap, playerMapping)
	dwr := remapPlayers(duelsStructValue(dw.duels), playerMapping)
	dwwr := remapPlayers(duelsStructValue(dw.duelsWithWorseWeapon), playerMapping)
	dwbr := remapPlayers(duelsStructValue(dw.duelsWithBetterWeapon), playerMapping)
	rer := remapPlayers(re.refragMap, playerMapping)
	cwr := remapPlayers(cw.clutchIndex, playerMapping)
	gdr := remapPlayers(gd.damage, playerMapping)
	smr := remapPlayers(sm.money, playerMapping)

	mor := remapTeams(mo.value(), teamMapping)
	rwr := remapTeams(rw.teams, teamMapping)
	rwer := remapTeams(rw.ecoRoundsTeams, teamMapping)
	rwpr := remapTeams(rw.pistolRoundsTeams, teamMapping)
	rweor := remapTeams(rw.ecoOpponentRoundsTeams, teamMapping)
	tgoalr := remapTeams(gs.defendedBombIndex, teamMapping)
	ctgoalr := remapTeams(gs.defusedBombIndex, teamMapping)

	features := map[string]map[string]float64{
		"flashes":               fsr,
		"flashAssists":          far,
		"entryFrags":            efr,
		"duelsWon":              dwr,
		"duelsWonWorse":         dwwr,
		"duelsWonBetter":        dwbr,
		"refrags":               rer,
		"clutchIndex":           cwr,
		"grenadeDamage":         gdr,
		"savedMoney":            smr,
		"mapOccupation":         mor,
		"roundsWon":             rwr,
		"ecoRoundsWon":          rwer,
		"pistolRoundsWon":       rwpr,
		"opponentEcoRoundsLost": rweor,
		"bombDefendedIndex":     tgoalr,
		"bombDefusedIndex":      ctgoalr,
	}

	tmp := strings.SplitAfter(p.Header().MapName, "/")
	mapName := tmp[len(tmp)-1]

	header := []string{}
	values := []string{}

	header = append(header, "id")
	header = append(header, "mapName")

	values = append(values, id)
	values = append(values, mapName)

	for key, entry := range names {
		for who, v := range entry {
			header = append(header, who+"_"+key)
			values = append(values, v)
		}
	}

	for key, entry := range features {
		for who, v := range entry {
			header = append(header, who+"_"+key)
			values = append(values, fmt.Sprintf("%f", v))
		}
	}

	csvWriter := csv.NewWriter(bufio.NewWriter(&buffer))

	csvWriter.Write(header)
	csvWriter.Write(values)
	csvWriter.Flush()

	if err != nil {
		return buffer, err
	}

	return buffer, err
}
