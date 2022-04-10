env GOOS=linux GOARCH=amd64 go build -o bin/rushb_analysis .
rm rushb_analysis.zip
zip -vr rushb_analysis.zip bin/ nav_meshes/ links.txt
