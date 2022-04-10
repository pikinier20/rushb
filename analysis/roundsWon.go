package main

import (
	dem "github.com/markus-wa/demoinfocs-golang/v2/pkg/demoinfocs"
	events "github.com/markus-wa/demoinfocs-golang/v2/pkg/demoinfocs/events"
)

func createRoundsWon() RoundsWonStatistics {
	roundsWon := RoundsWonStatistics{}
	roundsWon.teams = make(map[string]float64)
	roundsWon.pistolRoundsTeams = make(map[string]float64)
	roundsWon.ecoRoundsTeams = make(map[string]float64)
	roundsWon.ecoOpponentRoundsTeams = make(map[string]float64)
	return roundsWon
}

type RoundsWonStatistics struct {
	teams                  map[string]float64
	pistolRoundsTeams      map[string]float64
	ecoRoundsTeams         map[string]float64
	ecoOpponentRoundsTeams map[string]float64
}

func (f RoundsWonStatistics) register(p dem.Parser) {
	p.RegisterEventHandler(func(e events.RoundEnd) {
		if e.WinnerState == nil {
			return
		}
		old := f.teams[e.WinnerState.ClanName()]
		new := old + 1
		f.teams[e.WinnerState.ClanName()] = new
		if p.GameState().TotalRoundsPlayed() == 0 || p.GameState().TotalRoundsPlayed() == 15 {
			old := f.pistolRoundsTeams[e.WinnerState.ClanName()]
			new := old + 1
			f.pistolRoundsTeams[e.WinnerState.ClanName()] = new
		} else if e.WinnerState.RoundStartEquipmentValue() < 6000 && e.WinnerState.MoneySpentThisRound() < 6000 {
			old := f.ecoRoundsTeams[e.WinnerState.ClanName()]
			new := old + 1
			f.ecoRoundsTeams[e.WinnerState.ClanName()] = new

			old = f.ecoOpponentRoundsTeams[e.LoserState.ClanName()]
			new = old + 1
			f.ecoOpponentRoundsTeams[e.LoserState.ClanName()] = new
		}
	})
}
