package main

import (
	dem "github.com/markus-wa/demoinfocs-golang/v2/pkg/demoinfocs"
	events "github.com/markus-wa/demoinfocs-golang/v2/pkg/demoinfocs/events"
)

func createFlashStatistics() FlashStatistics {
	fs := FlashStatistics{}
	fs.flashMap = make(map[uint64]float64)
	fs.flashAssist = make(map[uint64]float64)
	return fs
}

type FlashStatistics struct {
	flashMap    map[uint64]float64
	flashAssist map[uint64]float64
}

func (f FlashStatistics) register(p dem.Parser) {
	p.RegisterEventHandler(func(e events.PlayerFlashed) {
		if e.FlashDuration().Seconds() > 4 {
			old := f.flashMap[e.Attacker.SteamID64]
			new := old + 1
			f.flashMap[e.Attacker.SteamID64] = new
		}
	})

	p.RegisterEventHandler(func(e events.Kill) {
		if e.Victim == nil || e.Killer == nil {
			return
		}
		if e.AssistedFlash {
			old := f.flashAssist[e.Assister.SteamID64]
			new := old + 1
			f.flashAssist[e.Assister.SteamID64] = new
		}
	})
}
