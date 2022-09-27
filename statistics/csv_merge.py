import pandas as pd
import glob
import os

path = r'./parsed_demos' # use your path
all_files = glob.glob(os.path.join(path , "*.csv"))

li = []

for filename in all_files:
    try:
        df = pd.read_csv(filename, index_col=None, header=0)
        li.append(df)
    except pd.errors.EmptyDataError:
        print(filename + " is empty")

frame = pd.concat(li, ignore_index=True).fillna(0)

frame.to_csv("merged_demos.csv", sep=";", index=False)