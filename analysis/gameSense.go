package main

import (
	dem "github.com/markus-wa/demoinfocs-golang/v2/pkg/demoinfocs"
	events "github.com/markus-wa/demoinfocs-golang/v2/pkg/demoinfocs/events"
)

func createGameSense() GameSense {
	gs := GameSense{}
	gs.points = make(map[uint64]int)
	return gs
}

type GameSense struct {
	points map[uint64]int
}

func (f GameSense) register(p dem.Parser) {
	p.RegisterEventHandler(func(e events.Kill) {
		if e.AttackerBlind || e.ThroughSmoke || e.IsWallBang() {
			old := f.points[e.Killer.SteamID64]
			new := old + 1
			f.points[e.Killer.SteamID64] = new
		}
	})
}
