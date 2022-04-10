package main

import (
	dem "github.com/markus-wa/demoinfocs-golang/v2/pkg/demoinfocs"
	"github.com/markus-wa/demoinfocs-golang/v2/pkg/demoinfocs/common"
	events "github.com/markus-wa/demoinfocs-golang/v2/pkg/demoinfocs/events"
)

func createRefragStatistics() RefragStatistics {
	refrag := RefragStatistics{}
	refrag.refragMap = make(map[uint64]float64)
	return refrag
}

type RefragStatistics struct {
	refragMap map[uint64]float64
}

type KillEntry struct {
	when float64
	who  *common.Player
}

func (f RefragStatistics) register(p dem.Parser) {
	killBuffer := make([]KillEntry, 0)
	onRoundStartFunc := func(e events.RoundStart) {
		killBuffer = make([]KillEntry, 0)
	}

	onKillFunc := func(e events.Kill) {
		if e.Victim == nil || e.Killer == nil {
			return
		}
		killEntry := KillEntry{}
		killEntry.who = e.Killer
		killEntry.when = p.CurrentTime().Seconds()

		killBuffer = append(killBuffer, killEntry)
		for _, v := range killBuffer {
			if v.who.SteamID64 == e.Victim.SteamID64 && (p.CurrentTime().Seconds()-v.when) < 5 {
				f.refragMap[e.Killer.SteamID64] = f.refragMap[e.Killer.SteamID64] + 1
			}
		}
	}
	p.RegisterEventHandler(onKillFunc)
	p.RegisterEventHandler(onRoundStartFunc)
}
