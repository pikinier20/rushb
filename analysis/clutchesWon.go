package main

import (
	dem "github.com/markus-wa/demoinfocs-golang/v2/pkg/demoinfocs"
	events "github.com/markus-wa/demoinfocs-golang/v2/pkg/demoinfocs/events"
)

func createClutchesWonStatistics() ClutchesWonStatistics {
	ef := ClutchesWonStatistics{clutchIndex: map[uint64]float64{}}
	return ef
}

type ClutchesWonStatistics struct {
	clutchIndex map[uint64]float64
}

func (f ClutchesWonStatistics) register(p dem.Parser) {
	possibleClutchPoints := map[uint64]float64{}
	p.RegisterEventHandler(func(e events.Kill) {
		if e.Victim == nil || e.Killer == nil {
			return
		}
		victimsAlive := 0
		for _, p := range e.Victim.TeamState.Members() {
			if p.IsAlive() {
				victimsAlive = victimsAlive + 1
			}
		}
		if victimsAlive == 2 {
			cp := .0
			for _, p := range e.Killer.TeamState.Members() {
				if p.IsAlive() {
					cp = cp + 1
				}
			}
			for _, p := range e.Victim.TeamState.Members() {
				if p.IsAlive() && p != e.Victim {
					possibleClutchPoints[p.SteamID64] = cp
				}
			}
		} else if victimsAlive == 1 {
			old := f.clutchIndex[e.Killer.SteamID64]
			new := old + possibleClutchPoints[e.Killer.SteamID64]
			f.clutchIndex[e.Killer.SteamID64] = new
		}
	})

	p.RegisterEventHandler(func(e events.RoundStart) {
		possibleClutchPoints = map[uint64]float64{}
	})

	p.RegisterEventHandler(func(e events.RoundEnd) {
		possibleClutchPoints = map[uint64]float64{}
	})

}
