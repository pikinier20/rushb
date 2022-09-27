# rushb

The project's goal is to predict the winner of a match in CS:GO. The repository contains code that:
 - gathers demos and statistics from HLTV.org
 - parses the demo with [demoinfocs-golang](https://github.com/markus-wa/demoinfocs-golang)
 - predicts the winner of a match based on gathered statistics
 
 The data and saved models are present in `assets.zip` file.
 
 To run prediction:
  - unpack the `assets.zip` file inside `statistics` folder
  - run the Jupyter Notebook and open `statistics/statistics_notebook.ipynb`

The project structure:
 - `analysis` - module written in Go that contains demo parser
 - `crawler` - module written in Scala that contains crawler for HLTV.org site
 - `downloader` - module written in Scala that downloads demos from HLTV.org
 - `statistics` - module written in Python that contains a code that predicts the winner of a match
 
All other modules are deprecated.
