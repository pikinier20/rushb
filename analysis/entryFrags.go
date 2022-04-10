package main

import (
	dem "github.com/markus-wa/demoinfocs-golang/v2/pkg/demoinfocs"
	events "github.com/markus-wa/demoinfocs-golang/v2/pkg/demoinfocs/events"
)

func createEntryFragsStatistics() EntryFragsStatistics {
	ef := EntryFragsStatistics{}
	ef.efMap = make(map[uint64]int)
	return ef
}

type EntryFragsStatistics struct {
	efMap map[uint64]int
}

func (f EntryFragsStatistics) register(p dem.Parser) {
	p.RegisterEventHandler(func(e events.Kill) {
		var allAlive bool
		allAlive = true
		for _, v := range append(p.GameState().TeamTerrorists().Members(), p.GameState().TeamCounterTerrorists().Members()...) {
			if !v.IsAlive() {
				allAlive = false
			}
		}
		if allAlive && p.GameState().IsMatchStarted() {
			f.efMap[e.Killer.SteamID64] = f.efMap[e.Killer.SteamID64] + 1
		}
	})
}
