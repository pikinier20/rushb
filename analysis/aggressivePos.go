package main

import (
	dem "github.com/markus-wa/demoinfocs-golang/v2/pkg/demoinfocs"
	"github.com/markus-wa/demoinfocs-golang/v2/pkg/demoinfocs/common"
	events "github.com/markus-wa/demoinfocs-golang/v2/pkg/demoinfocs/events"
)

func createAggressivePosStatistics() AggressivePosStatistics {
	ap := AggressivePosStatistics{}
	ap.apMap = make(map[uint64]int)
	return ap
}

type AggressivePosStatistics struct {
	apMap map[uint64]int
}

func (f AggressivePosStatistics) register(p dem.Parser) {
	p.RegisterEventHandler(func(e events.Footstep) {
		if isAggressive(p.Header().MapName, e.Player.LastPlaceName(), e.Player.Team == common.TeamTerrorists) {
			f.apMap[e.Player.SteamID64] = f.apMap[e.Player.SteamID64] + 1
		}
	})
}

// TODO
func isAggressive(map_name string, place_name string, t_side bool) bool {
	if map_name == "de_dust2" && place_name == "LongDoors" && !t_side {
		return true
	} else {
		return false
	}
}
