import subprocess

from bitarray import bitarray
import pandas as pd

for I in range(1):
    output = subprocess.check_output("java Suspicion -loadplayers random.txt", stderr=subprocess.PIPE)
    numplayers = 0
    DFexists = False
    guestNames = ["Buford Barnswallow", "Earl of Volesworthy", "Mildred Wellington", "Nadia Bwalya",
                  "Viola Chung", "Dr. Ashraf Najem", "Remy La Rocque", "Lily Nesbit", "Trudie Mudge",
                  "Stefano Laconi"]
    df = pd.DataFrame(columns=['g0', 'g1', 'g2', 'g3', 'g4', 'g5', '0ID', '1ID', '2ID', '3ID', '4ID', '5ID'])
    totalRand = [[0,0,0,0,0,0],
                 [0,0,0,0,0,0],
                 [0,0,0,0,0,0],
                 [0,0,0,0,0,0],
                 [0,0,0,0,0,0],
                 [0,0,0,0,0,0]]
    totalProb = [[0,0,0,0,0,0],
                 [0,0,0,0,0,0],
                 [0,0,0,0,0,0],
                 [0,0,0,0,0,0],
                 [0,0,0,0,0,0],
                 [0,0,0,0,0,0]]
    temp = 0
    randCount = 0
    probCount = 0
    curplayer = ""
    for line in output.splitlines():
        if line[1:]:
            line = line.decode('ascii')
            if "Loading player" in line:
                numplayers += 1
            elif "Actual player IDs" in line:
                playersIDs = (line.split(": ", 1)[1]).split(":")
                count = 0
                while count < numplayers:
                    currGuestNames = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
                    playersIDsplit = playersIDs[count].split(",")
                    index = guestNames.index(playersIDsplit[1])
                    currGuestNames[index] = 1
                    df[f"{playersIDsplit[0][len(playersIDsplit[0]) - 1]}ID"] = [int("".join(str(x) for x in
                                                                                            currGuestNames), 2)]
                    count += 1
            elif "knowledge" in line:
                curplayer = line.split(' knowledge')[0]
            elif ("bestPlayer.class" in line) & (temp < numplayers):
                temp += 1
                guesses = line.split(':')
                guesses.pop(len(guesses) - 1)
                df2 = pd.DataFrame(df.tail(1).copy())
                player0 = True
                player1 = True
                player2 = True
                player3 = True
                player4 = True
                player5 = True
                for guestSplit in guesses:
                    guestSplit1 = guestSplit.split(',', 1)
                    guess = guestSplit1[0][len(guestSplit1[0]) - 1]
                    currGuestNames = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0]

                    for guest in guestSplit1[1].split(","):
                        currGuestNames[guestNames.index(guest)] = 1

                    df2[f"g{guess}"] = int("".join(str(x) for x in currGuestNames), 2)
                    if int(guess) == 0:
                        player0 = False
                    if int(guess) == 1:
                        player1 = False
                    if int(guess) == 2:
                        player2 = False
                    if int(guess) == 3:
                        player3 = False
                    if int(guess) == 4:
                        player4 = False
                    if int(guess) == 5:
                        player5 = False

                if player0:
                    df2[f"g{0}"] = [df[f"{0}ID"][0]]
                if player1:
                    df2[f"g{1}"] = [df[f"{1}ID"][0]]
                if player2:
                    df2[f"g{2}"] = [df[f"{2}ID"][0]]
                if player3:
                    df2[f"g{3}"] = [df[f"{3}ID"][0]]
                if player4:
                    df2[f"g{4}"] = [df[f"{4}ID"][0]]
                if player5:
                    df2[f"g{5}"] = [df[f"{5}ID"][0]]

                if not DFexists:
                    df = pd.DataFrame(df2)
                    DFexists = True
                else:
                    df = pd.concat([df, df2], ignore_index=True)
            elif "random" in line:
                randline = line.split(':',1)[1]
                for randguess in randline.split(':'):
                    currGuestNames = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
                    randPlayer = randguess.split(',', 1)[0][len(randguess.split(',', 1)[0]) - 1]
                    index = guestNames.index(randguess.split(',', 1)[1].split(',',1)[0])
                    currGuestNames[index] = 1
                    totalRand[randCount][int(randPlayer)] = int("".join(str(x) for x in currGuestNames), 2)
                index = 0
                for search in totalRand[randCount]:
                    if search == 0:
                        totalRand[randCount][index] = df[f"{index}ID"][0]
                    index += 1
                randCount += 1
            elif "sorted" in line:
                randline = line.split(':',1)[1]
                for randguess in randline.split(':'):
                    currGuestNames = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
                    randPlayer = randguess.split(',', 1)[0][len(randguess.split(',', 1)[0]) - 1]
                    index = guestNames.index(randguess.split(',', 1)[1].split(',',1)[0])
                    currGuestNames[index] = 1
                    totalProb[probCount][int(randPlayer)] = int("".join(str(x) for x in currGuestNames), 2)
                index = 0
                for search in totalProb[probCount]:
                    if search == 0:
                        totalProb[probCount][index] = df[f"{index}ID"][0]
                    index += 1
                probCount += 1

    #     print(line)
    # print(df)
    # df = df.drop_duplicates(ignore_index=True)
    # df = df.iloc[-50:, :]
    # print(df)
    df.to_csv("results17.csv", index=False)
    # df.to_csv("results18.csv", index=False, mode='a', header=False)