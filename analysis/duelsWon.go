package main

import (
	dem "github.com/markus-wa/demoinfocs-golang/v2/pkg/demoinfocs"
	events "github.com/markus-wa/demoinfocs-golang/v2/pkg/demoinfocs/events"
)

func createDuelsWonStatistics() DuelsWonStatistics {
	ef := DuelsWonStatistics{}
	ef.duels = make(map[uint64]DuelsStruct)
	ef.duelsWithBetterWeapon = make(map[uint64]DuelsStruct)
	ef.duelsWithWorseWeapon = make(map[uint64]DuelsStruct)
	return ef
}

type DuelsWonStatistics struct {
	duels                 map[uint64]DuelsStruct
	duelsWithWorseWeapon  map[uint64]DuelsStruct
	duelsWithBetterWeapon map[uint64]DuelsStruct
}

type DuelsStruct struct {
	count int
	won   int
}

func duelsStructValue(v map[uint64]DuelsStruct) map[uint64]float64 {
	res := make(map[uint64]float64)
	for i, e := range v {
		res[i] = e.value()
	}

	return res
}

func (v DuelsStruct) value() float64 {
	return float64(v.won) / float64(v.count)
}

func (f DuelsWonStatistics) register(p dem.Parser) {
	p.RegisterEventHandler(func(e events.Kill) {
		if e.Victim == nil || e.Killer == nil {
			return
		}
		if e.Killer != nil && e.Victim != nil && p.GameState().IsMatchStarted() && e.Victim.HasSpotted(e.Killer) && e.Assister == nil && e.AssistedFlash == false {
			f.duels[e.Killer.SteamID64] = DuelsStruct{count: f.duels[e.Killer.SteamID64].count + 1, won: f.duels[e.Killer.SteamID64].won + 1}
			f.duels[e.Victim.SteamID64] = DuelsStruct{count: f.duels[e.Victim.SteamID64].count + 1, won: f.duels[e.Victim.SteamID64].won}

			if e.Weapon.Class() <= 4 && e.Weapon.Class() < e.Victim.ActiveWeapon().Class() {
				f.duelsWithWorseWeapon[e.Killer.SteamID64] = DuelsStruct{count: f.duelsWithWorseWeapon[e.Killer.SteamID64].count + 1, won: f.duelsWithWorseWeapon[e.Killer.SteamID64].won + 1}
				f.duelsWithBetterWeapon[e.Victim.SteamID64] = DuelsStruct{count: f.duelsWithBetterWeapon[e.Victim.SteamID64].count + 1, won: f.duelsWithBetterWeapon[e.Victim.SteamID64].won}
			}
			if e.Weapon.Class() <= 4 && e.Weapon.Class() > e.Victim.ActiveWeapon().Class() {
				f.duelsWithBetterWeapon[e.Killer.SteamID64] = DuelsStruct{count: f.duelsWithBetterWeapon[e.Killer.SteamID64].count + 1, won: f.duelsWithBetterWeapon[e.Killer.SteamID64].won + 1}
				f.duelsWithWorseWeapon[e.Victim.SteamID64] = DuelsStruct{count: f.duelsWithWorseWeapon[e.Victim.SteamID64].count + 1, won: f.duelsWithWorseWeapon[e.Victim.SteamID64].won}
			}
		}
	})
}
