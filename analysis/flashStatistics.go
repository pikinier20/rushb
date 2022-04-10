package main

import (
	dem "github.com/markus-wa/demoinfocs-golang/v2/pkg/demoinfocs"
	events "github.com/markus-wa/demoinfocs-golang/v2/pkg/demoinfocs/events"
)

func createFlashStatistics() FlashStatistics {
	fs := FlashStatistics{}
	fs.flashMap = make(map[uint64]int)
	return fs
}

type FlashStatistics struct {
	flashMap map[uint64]int
}

func (f FlashStatistics) register(p dem.Parser) {
	p.RegisterEventHandler(func(e events.PlayerFlashed) {
		if p.GameState().IsMatchStarted() {
			f.flashMap[e.Attacker.SteamID64] = f.flashMap[e.Attacker.SteamID64] + 1
		}
	})
}
