package main

import (
	dem "github.com/markus-wa/demoinfocs-golang/v2/pkg/demoinfocs"
	"github.com/markus-wa/demoinfocs-golang/v2/pkg/demoinfocs/common"
	events "github.com/markus-wa/demoinfocs-golang/v2/pkg/demoinfocs/events"
)

func createGrenadeDamage() GrenadeDamage {
	gd := GrenadeDamage{}
	gd.damage = make(map[uint64]float64)
	return gd
}

type GrenadeDamage struct {
	damage map[uint64]float64
}

func (f GrenadeDamage) register(p dem.Parser) {
	p.RegisterEventHandler(func(e events.PlayerHurt) {
		if e.Weapon.Class() == common.EqClassGrenade {
			old := f.damage[e.Attacker.SteamID64]
			new := old + float64(e.HealthDamageTaken)
			f.damage[e.Attacker.SteamID64] = new
		}
	})
}
