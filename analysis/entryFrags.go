package main

import (
	dem "github.com/markus-wa/demoinfocs-golang/v2/pkg/demoinfocs"
	events "github.com/markus-wa/demoinfocs-golang/v2/pkg/demoinfocs/events"
)

func createEntryFragsStatistics() EntryFragsStatistics {
	ef := EntryFragsStatistics{}
	ef.efMap = make(map[uint64]float64)
	return ef
}

type EntryFragsStatistics struct {
	efMap map[uint64]float64
}

func (f EntryFragsStatistics) register(p dem.Parser) {
	p.RegisterEventHandler(func(e events.Kill) {
		if e.Victim == nil || e.Killer == nil {
			return
		}
		var allAlive bool
		allAlive = true
		for _, v := range append(p.GameState().TeamTerrorists().Members(), p.GameState().TeamCounterTerrorists().Members()...) {
			if v != e.Victim && !v.IsAlive() {
				allAlive = false
			}
		}
		if allAlive && p.GameState().IsMatchStarted() {
			f.efMap[e.Killer.SteamID64] = f.efMap[e.Killer.SteamID64] + 1
		}
	})
}
