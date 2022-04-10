package main

import (
	"fmt"
	"os"

	dem "github.com/markus-wa/demoinfocs-golang/v2/pkg/demoinfocs"
)

func main() {
	f, err := os.Open("vitality-vs-astralis-m3-dust2.dem")
	if err != nil {
		panic(err)
	}
	defer f.Close()

	p := dem.NewParser(f)
	defer p.Close()

	p.ParseHeader()

	fmt.Printf("%s\n", p.Header().MapName)

	fs := createFlashStatistics()
	ef := createEntryFragsStatistics()
	dw := createDuelsWonStatistics()
	ap := createAggressivePosStatistics()
	re := createRefragStatistics()

	fs.register(p)
	ef.register(p)
	dw.register(p)
	ap.register(p)
	re.register(p)

	// Parse to end
	err = p.ParseToEnd()

	fmt.Printf("flashMap: %v\n", fs.flashMap)
	fmt.Printf("ef.efMap: %v\n", ef.efMap)
	fmt.Printf("ap.apMap: %v\n", ap.apMap)
	for key, value := range dw.dwMap {
		fmt.Printf("%d: %f\n", key, value.value())
		fmt.Printf("%d: %d\n", key, value.count)
	}
	fmt.Printf("re.refragMap: %v\n", re.refragMap)

	if err != nil {
		panic(err)
	}
}
