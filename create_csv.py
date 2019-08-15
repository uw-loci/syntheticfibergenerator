#!/usr/bin/env python3

import json
import math
import sys


def create_csv(json_filename, csv_filename):
    print("Creating CSV...")

    # Determine angle, length, width, and straightness
    lines = ["Angle,Length,Mean Width,Straightness\n"]

    with open(json_filename, "r") as json_file:
        fibers = json.load(json_file)["fibers"]
        for fiber in fibers:
            params = fiber["params"]

            dx = params["end"]["x"] - params["start"]["x"]
            dy = params["end"]["y"] - params["start"]["y"]
            angle = math.degrees(-math.atan2(dy, dx))

            length = params["nSegments"] * params["segmentLength"]

            meanWidth = 0.0
            for width in fiber["widths"]:
                meanWidth += width / len(fiber["widths"])

            straightness = params["straightness"]

            lines.append("{},{},{},{}\n".format(
                angle, length, meanWidth, straightness))

    with open(csv_filename, "w") as csv_file:
        csv_file.writelines(lines)
    print("Done")


if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("Usage: ./create_csv.py <json-filename> <csv-filename>")
        exit()
    create_csv(sys.argv[1], sys.argv[2])
