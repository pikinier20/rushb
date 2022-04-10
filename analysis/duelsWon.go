package main

import (
	dem "github.com/markus-wa/demoinfocs-golang/v2/pkg/demoinfocs"
	events "github.com/markus-wa/demoinfocs-golang/v2/pkg/demoinfocs/events"
)

func createDuelsWonStatistics() DuelsWonStatistics {
	ef := DuelsWonStatistics{}
	ef.dwMap = make(map[uint64]DuelsStruct)
	return ef
}

type DuelsWonStatistics struct {
	dwMap map[uint64]DuelsStruct
}

type DuelsStruct struct {
	count int
	won   int
}

func (v DuelsStruct) value() float32 {
	return float32(v.won) / float32(v.count)
}

func (f DuelsWonStatistics) register(p dem.Parser) {
	p.RegisterEventHandler(func(e events.Kill) {
		if e.Killer != nil && e.Victim != nil && p.GameState().IsMatchStarted() && e.Victim.HasSpotted(e.Killer) && e.Assister == nil {
			f.dwMap[e.Killer.SteamID64] = DuelsStruct{count: f.dwMap[e.Killer.SteamID64].count + 1, won: f.dwMap[e.Killer.SteamID64].won + 1}
			f.dwMap[e.Victim.SteamID64] = DuelsStruct{count: f.dwMap[e.Victim.SteamID64].count + 1, won: f.dwMap[e.Victim.SteamID64].won}
		}
	})
}
