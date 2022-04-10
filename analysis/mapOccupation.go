package main

import (
	"math"
	"os"
	"strings"

	"github.com/golang/geo/r3"
	dem "github.com/markus-wa/demoinfocs-golang/v2/pkg/demoinfocs"
	events "github.com/markus-wa/demoinfocs-golang/v2/pkg/demoinfocs/events"
	"github.com/mrazza/gonav"
)

func createMapOccupation() MapOccupation {
	ap := MapOccupation{}
	ap.occupations = make(map[string]float64)
	return ap
}

type MapOccupation struct {
	occupations map[string]float64
}

type MapRepr struct {
	places map[string]PlaceRepr
}

type PlaceRepr struct {
	size       float64
	diameter   float64
	occupation int
	center     r3.Vector
	t_count    int
	ct_count   int
}

func (f MapOccupation) value() map[string]float64 {
	sum := 1.0
	for _, o := range f.occupations {
		sum = sum + o
	}
	for i, _ := range f.occupations {
		v := f.occupations[i] / sum
		f.occupations[i] = v
	}
	return f.occupations
}

func (f MapOccupation) register(p dem.Parser) {
	tmp := strings.SplitAfter(p.Header().MapName, "/")
	mapName := tmp[len(tmp)-1]
	mapRepr := createMapRepr(mapName)
	var lastTime float64
	lastTime = -1000

	p.RegisterEventHandler(func(e events.Footstep) {
		if p.CurrentTime().Seconds()-lastTime > 5 {
			// Clearing players count
			for _, place := range mapRepr.places {
				place.t_count = 0
				place.ct_count = 0
			}

			lastTime = p.CurrentTime().Seconds()
			for _, player := range p.GameState().TeamTerrorists().Members() {
				if player.IsAlive() {
					placeName := player.LastPlaceName()
					place := mapRepr.places[placeName]
					if player.PositionEyes().Distance(place.center) <= place.diameter {
						place.t_count = place.t_count + 1
					}
					mapRepr.places[placeName] = place
				}
			}

			for _, player := range p.GameState().TeamCounterTerrorists().Members() {
				if player.IsAlive() {
					placeName := player.LastPlaceName()
					place := mapRepr.places[placeName]
					if player.PositionEyes().Distance(place.center) <= place.diameter {
						place.ct_count = place.ct_count + 1
					}
					mapRepr.places[placeName] = place
				}
			}

			for _, place := range mapRepr.places {
				if place.t_count > 0 && place.ct_count == 0 {
					place.occupation = 1
				} else if place.ct_count > 0 && place.t_count == 0 {
					place.occupation = -1
				} else if place.ct_count != 0 && place.t_count != 0 {
					place.occupation = 0
				}

				if place.occupation == 1 {
					oldOcc := f.occupations[p.GameState().TeamTerrorists().ClanName()]
					newOcc := oldOcc + place.size
					f.occupations[p.GameState().TeamTerrorists().ClanName()] = newOcc
				} else if place.occupation == -1 {
					oldOcc := f.occupations[p.GameState().TeamCounterTerrorists().ClanName()]
					newOcc := oldOcc + place.size
					f.occupations[p.GameState().TeamCounterTerrorists().ClanName()] = newOcc
				}
			}
		}
	})

	p.RegisterEventHandler(func(e events.RoundStart) {
		for _, place := range mapRepr.places {
			place.occupation = 0
		}
	})
}

func createMapRepr(mapName string) MapRepr {
	f, _ := os.Open("nav_meshes/" + mapName + ".nav")
	parser := gonav.Parser{Reader: f}
	mesh, _ := parser.Parse()
	mapRepr := MapRepr{}
	mapRepr.places = make(map[string]PlaceRepr)
	for _, place := range mesh.Places {
		name := place.Name
		var minX float64
		var maxX float64
		var minY float64
		var maxY float64
		minX = math.Inf(1)
		minY = math.Inf(1)
		maxX = math.Inf(-1)
		maxY = math.Inf(-1)
		var surface float64
		for a := range place.Areas {
			pos := place.Areas[a].GetCenter()
			if minX > float64(pos.X) {
				minX = float64(pos.X)
			}
			if maxX < float64(pos.X) {
				maxX = float64(pos.X)
			}
			if maxY < float64(pos.Y) {
				maxY = float64(pos.Y)
			}
			if minY > float64(pos.Y) {
				minY = float64(pos.Y)
			}
			surface = surface + float64(place.Areas[a].GetRoughSquaredArea())
		}
		width := maxX - minX
		height := maxY - minY
		widthThreshold := width / 2.8
		heightThreshold := height / 2.8
		diameter := math.Sqrt(math.Pow(widthThreshold, 2) + math.Pow(heightThreshold, 2))
		center, _ := place.GetEstimatedCenter()
		mapRepr.places[name] = PlaceRepr{
			diameter: diameter,
			size:     surface,
			center: r3.Vector{
				X: float64(center.X),
				Y: float64(center.Y),
				Z: float64(center.Z),
			},
		}
	}
	return mapRepr
}
