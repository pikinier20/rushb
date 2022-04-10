package main

import (
	dem "github.com/markus-wa/demoinfocs-golang/v2/pkg/demoinfocs"
	events "github.com/markus-wa/demoinfocs-golang/v2/pkg/demoinfocs/events"
)

func createGoalStatistics() GoalStatistics {
	return GoalStatistics{
		defendedBombIndex: make(map[string]float64),
		defusedBombIndex:  make(map[string]float64),
	}
}

type GoalStatistics struct {
	defendedBombIndex map[string]float64
	defusedBombIndex  map[string]float64
}

func (f GoalStatistics) register(p dem.Parser) {
	tAlive := -1
	ctAlive := -1

	roundEndEvent := func(e events.RoundEnd) {
		if tAlive != -1 && ctAlive != -1 && (e.Reason == events.RoundEndReasonTargetBombed || e.Reason == events.RoundEndReasonTerroristsWin) {
			teamName := p.GameState().TeamTerrorists().ClanName()
			opponentTeamname := p.GameState().TeamCounterTerrorists().ClanName()
			old := f.defendedBombIndex[teamName]
			new := old + (float64(ctAlive) / float64(tAlive))
			f.defendedBombIndex[teamName] = new

			old = f.defusedBombIndex[opponentTeamname]
			new = old - (float64(ctAlive) / float64(tAlive))
			f.defusedBombIndex[opponentTeamname] = new
		}

		if tAlive != -1 && ctAlive != -1 && (e.Reason == events.RoundEndReasonBombDefused) {
			teamName := p.GameState().TeamCounterTerrorists().ClanName()
			opponentTeamname := p.GameState().TeamTerrorists().ClanName()
			old := f.defendedBombIndex[opponentTeamname]
			new := old - (float64(tAlive) / float64(ctAlive))
			f.defendedBombIndex[opponentTeamname] = new

			old = f.defusedBombIndex[teamName]
			new = old + (float64(tAlive) / float64(ctAlive))
			f.defusedBombIndex[teamName] = new
		}
		tAlive = -1
		ctAlive = -1
	}

	roundStartEvent := func(e events.RoundStart) {
		tAlive = -1
		ctAlive = -1
	}

	bombPlantedEvent := func(e events.BombPlanted) {
		tAlive = 0
		ctAlive = 0
		for _, player := range p.GameState().TeamTerrorists().Members() {
			if player.IsAlive() {
				tAlive = tAlive + 1
			}
		}

		for _, player := range p.GameState().TeamCounterTerrorists().Members() {
			if player.IsAlive() {
				ctAlive = ctAlive + 1
			}
		}
	}

	p.RegisterEventHandler(roundStartEvent)
	p.RegisterEventHandler(bombPlantedEvent)
	p.RegisterEventHandler(roundEndEvent)
}
