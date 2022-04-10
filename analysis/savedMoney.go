package main

import (
	dem "github.com/markus-wa/demoinfocs-golang/v2/pkg/demoinfocs"
	events "github.com/markus-wa/demoinfocs-golang/v2/pkg/demoinfocs/events"
)

func createSavedMoney() SavedMoney {
	sm := SavedMoney{}
	sm.money = make(map[uint64]float64)
	return sm
}

type SavedMoney struct {
	money map[uint64]float64
}

func (f SavedMoney) register(p dem.Parser) {
	p.RegisterEventHandler(func(e events.RoundEnd) {
		if e.LoserState == nil {
			return
		}
		for _, p := range e.LoserState.Members() {
			if p.IsAlive() {
				old := f.money[p.SteamID64]
				new := old + float64(p.EquipmentValueCurrent())
				f.money[p.SteamID64] = new
			}
		}
	})
}
