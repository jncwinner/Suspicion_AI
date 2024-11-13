#!/usr/bin/python

import pickle
import numpy as np
from sklearn.preprocessing import OneHotEncoder
import pandas as pd
from sklearn.metrics import mean_squared_error
import random
from sklearn.metrics import r2_score
from sklearn import preprocessing
from sklearn.metrics import classification_report, confusion_matrix, accuracy_score
import sys
import subprocess


def main(argv):
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

        print(line)
    print(df)
    # print(output)

    Xencoder = OneHotEncoder(categories='auto')
    Yencoder = OneHotEncoder(categories='auto')
    Yencoder2 = OneHotEncoder(categories='auto')
    Yencoder3 = OneHotEncoder(categories='auto')

    testList = [
                [1, 1, 1, 1, 1, 1],
                [2, 2, 2, 2, 2, 2],
                [4, 4, 4, 4, 4, 4],
                [8, 8, 8, 8, 8, 8],
                [16, 16, 16, 16, 16, 16],
                [32, 32, 32, 32, 32, 32],
                [64, 64, 64, 64, 64, 64],
                [128, 128, 128, 128, 128, 128],
                [256, 256, 256, 256, 256, 256],
                [512, 512, 512, 512, 512, 512]]
    testList2 = [[0, 0, 0, 0, 0, 0],
                [1, 1, 1, 1, 1, 1],
                [2, 2, 2, 2, 2, 2],
                [4, 4, 4, 4, 4, 4],
                [8, 8, 8, 8, 8, 8],
                [16, 16, 16, 16, 16, 16],
                [32, 32, 32, 32, 32, 32],
                [64, 64, 64, 64, 64, 64],
                [128, 128, 128, 128, 128, 128],
                [256, 256, 256, 256, 256, 256],
                [512, 512, 512, 512, 512, 512]]
    testList3 = [[1, 0, 0, 0, 0, 0],
                [0, 1, 0, 0, 0, 0],
                [0, 0, 1, 0, 0, 0],
                [0, 0, 0, 1, 0, 0],
                [0, 0, 0, 0, 1, 0],
                [0, 0, 0, 0, 0, 1],
                [2, 0, 0, 0, 0, 0],
                [0, 2, 0, 0, 0, 0],
                [0, 0, 2, 0, 0, 0],
                [0, 0, 0, 2, 0, 0],
                [0, 0, 0, 0, 2, 0],
                [0, 0, 0, 0, 0, 2],
                [4, 0, 0, 0, 0, 0],
                [0, 4, 0, 0, 0, 0],
                [0, 0, 4, 0, 0, 0],
                [0, 0, 0, 4, 0, 0],
                [0, 0, 0, 0, 4, 0],
                [0, 0, 0, 0, 0, 4],
                [8, 0, 0, 0, 0, 0],
                [0, 8, 0, 0, 0, 0],
                [0, 0, 8, 0, 0, 0],
                [0, 0, 0, 8, 0, 0],
                [0, 0, 0, 0, 8, 0],
                [0, 0, 0, 0, 0, 8],
                [16, 0, 0, 0, 0, 0],
                [0, 16, 0, 0, 0, 0],
                [0, 0, 16, 0, 0, 0],
                [0, 0, 0, 16, 0, 0],
                [0, 0, 0, 0, 16, 0],
                [0, 0, 0, 0, 0, 16],
                [32, 0, 0, 0, 0, 0],
                [0, 32, 0, 0, 0, 0],
                [0, 0, 32, 0, 0, 0],
                [0, 0, 0, 32, 0, 0],
                [0, 0, 0, 0, 32, 0],
                [0, 0, 0, 0, 0, 32],
                [64, 0, 0, 0, 0, 0],
                [0, 64, 0, 0, 0, 0],
                [0, 0, 64, 0, 0, 0],
                [0, 0, 0, 64, 0, 0],
                [0, 0, 0, 0, 64, 0],
                [0, 0, 0, 0, 0, 64],
                [128, 0, 0, 0, 0, 0],
                [0, 128, 0, 0, 0, 0],
                [0, 0, 128, 0, 0, 0],
                [0, 0, 0, 128, 0, 0],
                [0, 0, 0, 0, 128, 0],
                [0, 0, 0, 0, 0, 128],
                [256, 0, 0, 0, 0, 0],
                [0, 256, 0, 0, 0, 0],
                [0, 0, 256, 0, 0, 0],
                [0, 0, 0, 256, 0, 0],
                [0, 0, 0, 0, 256, 0],
                [0, 0, 0, 0, 0, 256],
                [512, 0, 0, 0, 0, 0],
                [0, 512, 0, 0, 0, 0],
                [0, 0, 512, 0, 0, 0],
                [0, 0, 0, 512, 0, 0],
                [0, 0, 0, 0, 512, 0],
                [0, 0, 0, 0, 0, 512],
                ]
    newList = []
    for i in range(1024):
        newList.append([i, i, i, i, i, i])

    Yencoder.fit(testList)
    Xencoder.fit(newList)
    Yencoder2.fit(testList2)
    Yencoder3.fit(testList3)


    # loaded_prediction_model = pickle.load(open("../P1/deliver.p1/finalized_6p_model29.sav", "rb"))
    # loaded_prediction_model2 = pickle.load(open("../P1/deliver.p1/finalized_6p_model30.sav", "rb"))
    # loaded_prediction_model3 = pickle.load(open("../P1/deliver.p1/finalized_6p_model31.sav", "rb"))
    # loaded_prediction_model4 = pickle.load(open("../P1/deliver.p1/finalized_6p_model32.sav", "rb"))
    # loaded_prediction_model5 = pickle.load(open("../P1/deliver.p1/finalized_6p_model33.sav", "rb"))
    # loaded_prediction_model6 = pickle.load(open("../P1/deliver.p1/finalized_6p_model34.sav", "rb"))
    # loaded_prediction_model7 = pickle.load(open("../P1/deliver.p1/finalized_6p_model35.sav", "rb"))
    # loaded_prediction_model8 = pickle.load(open("../P1/deliver.p1/finalized_6p_model36.sav", "rb"))
    # loaded_prediction_model9 = pickle.load(open("../P1/deliver.p1/finalized_6p_model37.sav", "rb"))
    # loaded_prediction_model10 = pickle.load(open("../P1/deliver.p1/finalized_6p_model38.sav", "rb"))
    # loaded_prediction_model11 = pickle.load(open("../P1/deliver.p1/finalized_6p_model39.sav", "rb"))
    loaded_prediction_model12 = pickle.load(open("../P1/deliver.p1/finalized_6p_model40.sav", "rb"))

    X2 = np.array(df.iloc[:, 0:6])
    Y2 = np.array(df.iloc[:, 6:12])
    Y2_train_dataset = Y2.reshape((-1, 6))

    X2_train_dataset = X2.reshape((-1, 6))

    Y2_onehot = Yencoder.transform(Y2).toarray()
    X2_onehot = Xencoder.transform(X2).toarray()

    # prediction = loaded_prediction_model.predict(X2_onehot)
    # prediction2 = loaded_prediction_model2.predict(X2_onehot)
    # prediction3 = loaded_prediction_model3.predict(X2_onehot)
    # prediction4 = loaded_prediction_model4.predict(X2_onehot)
    # prediction5 = loaded_prediction_model5.predict(X2_onehot)
    # prediction6 = loaded_prediction_model6.predict(X2_onehot)
    # prediction7 = loaded_prediction_model7.predict(X2_onehot)
    # prediction8 = loaded_prediction_model8.predict(X2_onehot)
    # prediction9 = loaded_prediction_model9.predict(X2_onehot)
    # prediction10 = loaded_prediction_model10.predict(X2_onehot)
    # prediction11 = loaded_prediction_model11.predict(X2_onehot)
    prediction12 = loaded_prediction_model12.predict(X2_onehot)

    # np.set_printoptions(threshold=sys.maxsize)
    print(X2)
    # print(mean_squared_error(prediction, Y2_onehot))
    # print(np.array(prediction).reshape(36,10))
    print("true guess IDs")
    print(Y2)
    print("\nrandom sorted guess")
    print(np.array(totalRand))
    print(np.mean(totalRand != Y2))
    print("\nprobability sorted guess")
    print(np.array(totalProb))
    print(np.mean(totalProb != Y2))
    print("\nML model sorted guess")
    # Xdecoded = Yencoder.inverse_transform(prediction)
    # print(Xdecoded)
    # print(np.mean(Xdecoded != Y2))
    # print("\nML model sorted guess")
    # Xdecoded = Yencoder.inverse_transform(prediction2)
    # print(Xdecoded)
    # print(np.mean(Xdecoded != Y2))
    # print("\nML model sorted guess")
    # Xdecoded = Yencoder.inverse_transform(prediction3)
    # print(Xdecoded)
    # print(np.mean(Xdecoded != Y2))
    # print("\nML model sorted guess")
    # Xdecoded = Yencoder.inverse_transform(prediction4)
    # print(Xdecoded)
    # print(np.mean(Xdecoded != Y2))
    # print("\nML model sorted guess")
    # Xdecoded = Yencoder.inverse_transform(prediction5)
    # print(Xdecoded)
    # print(np.mean(Xdecoded != Y2))
    # print("\nML model sorted guess")
    # Xdecoded = Yencoder.inverse_transform(prediction6)
    # print(Xdecoded)
    # print(np.mean(Xdecoded != Y2))
    # print("\nML model sorted guess")
    # Xdecoded = Yencoder2.inverse_transform(prediction7)
    # print(Xdecoded)
    # print(np.mean(Xdecoded != Y2))
    # print("\nML model sorted guess")
    # Xdecoded = Yencoder3.inverse_transform(prediction8)
    # print(Xdecoded)
    # print(np.mean(Xdecoded != Y2))
    # print("\nML model sorted guess")
    # Xdecoded = Yencoder3.inverse_transform(prediction9)
    # print(Xdecoded)
    # print(np.mean(Xdecoded != Y2))
    # print("\nML model sorted guess")
    # Xdecoded = Yencoder3.inverse_transform(prediction10)
    # print(Xdecoded)
    # print(np.mean(Xdecoded != Y2))
    # print("\nML model sorted guess")
    # Xdecoded = Yencoder3.inverse_transform(prediction11)
    # print(Xdecoded)
    # print(np.mean(Xdecoded != Y2))
    print("\nML model sorted guess")
    Xdecoded = Yencoder3.inverse_transform(prediction12)
    print(Xdecoded)
    print(np.mean(Xdecoded != Y2))
    # X2 = np.array(df.iloc[:1, 0:6])
    # Y2 = np.array(df.iloc[:1, 6:12])
    # Y2_train_dataset = Y2.reshape((-1, 6))
    #
    # X2_train_dataset = X2.reshape((-1, 6))
    #
    # Y2_onehot = Yencoder.transform(Y2).toarray()
    # X2_onehot = Xencoder.transform(X2).toarray()
    #
    # prediction = loaded_prediction_model.predict(X2_onehot)
    # print(mean_squared_error(prediction, Y2_onehot))
    # decoded = Yencoder.inverse_transform(prediction)
    # print(decoded)
    # decoded = Yencoder.inverse_transform(Y2_onehot)
    # print(decoded)
    #
    # X2 = np.array(df.iloc[1:2, 0:6])
    # Y2 = np.array(df.iloc[1:2, 6:12])
    #
    # Y2_onehot = Yencoder.transform(Y2).toarray()
    # X2_onehot = Xencoder.transform(X2).toarray()
    #
    # prediction = loaded_prediction_model.predict(X2_onehot)
    # print(mean_squared_error(prediction, Y2_onehot))
    # decoded = Yencoder.inverse_transform(prediction)
    # print(decoded)
    # decoded = Yencoder.inverse_transform(Y2_onehot)
    # print(decoded)

if __name__ == "__main__":
    main(sys.argv[1:])
