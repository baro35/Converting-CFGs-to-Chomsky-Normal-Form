import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class CFGToCNF {
	static Map<String, List<String>> grammarMap = new LinkedHashMap<>();
	static String epselonFound = "";
	static int lineCount = 0;
	static int terminalCount = 0;

	public static void readToString() {
		try {
			File myObj = new File("CFG.txt");
			Scanner myReader = new Scanner(myObj);
			while (myReader.hasNextLine()) {
				String data = myReader.nextLine();
				lineCount++;
				convertToMap(data);

			}
			myReader.close();
		} catch (FileNotFoundException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
	}

	public static void convertToMap(String data) {
		String[] tempString = data.split("-|\\|");
		String variable = tempString[0].trim();

		String[] production = Arrays.copyOfRange(tempString, 1, tempString.length);
		List<String> productionList = new ArrayList<String>();

		// delete the empty space
		for (int k = 0; k < production.length; k++) {
			production[k] = production[k].trim();
		}

		// add Right-hand side into ArrayList
		for (int j = 0; j < production.length; j++) {
			productionList.add(production[j]);
		}

		//insert element into map
		grammarMap.put(variable, productionList);

	}

	public static void printMap() {
		Iterator iterator = grammarMap.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry pair = (Map.Entry) iterator.next();
			System.out.println(pair.getKey() + " -> " + pair.getValue());
		}

		System.out.println("");
	}

	public static void removeEpsilon() {
		Iterator itr = grammarMap.entrySet().iterator();
		Iterator itr2 = grammarMap.entrySet().iterator();

		while (itr.hasNext()) {
			Map.Entry entry = (Map.Entry) itr.next();
			ArrayList<String> symbolList = (ArrayList<String>) entry.getValue();

			if (symbolList.contains("€")) {
				if (symbolList.size() > 1) {
					symbolList.remove("€");
					epselonFound = entry.getKey().toString();


				} else {

					// remove if less than 1
					epselonFound = entry.getKey().toString();
					grammarMap.remove(epselonFound);
				}
			}
		}

		// find B and eliminate them
		while (itr2.hasNext()) {

			Map.Entry entry = (Map.Entry) itr2.next();
			ArrayList<String> symbolList2 = (ArrayList<String>) entry.getValue();

			for (int i = 0; i < symbolList2.size(); i++) {
				String temp = symbolList2.get(i);

				for (int j = 0; j < temp.length(); j++) {
					if (epselonFound.equals(Character.toString(temp.charAt(j)))) {

						if(temp.length() == 1) { //  A-> .... | (key includes epsilon) | ...

							if (!grammarMap.get(entry.getKey().toString()).contains("€")) {
								grammarMap.get(entry.getKey().toString()).add("€");
							}
						}
						else if (temp.length() == 2) {//If the symbol length is 2, delete the epsilon found symbol and add the new symbol

							// remove specific character in string
							temp = temp.replace(epselonFound, "");

							if (!grammarMap.get(entry.getKey().toString()).contains(temp)) {
								grammarMap.get(entry.getKey().toString()).add(temp);
							}

						} else {//If the symbol length is more than 2, delete the epsilon found symbol and add the new symbol,
							//In continue, other possibilities will calculate in while loop

							String deletedTemp = new StringBuilder(temp).deleteCharAt(j).toString();

							if (!grammarMap.get(entry.getKey().toString()).contains(deletedTemp)) {
								grammarMap.get(entry.getKey().toString()).add(deletedTemp);
							}

						}
					}
				}
			}
		}
	}

	public static void unitProduction() {
		Iterator<Map.Entry<String,List<String>>> iterator = grammarMap.entrySet().iterator();
		String keyToAddSymbols = "";


		while (iterator.hasNext()) {

			Map.Entry entry = (Map.Entry) iterator.next(); //Get entries
			ArrayList<String> keySet = new ArrayList<String>(grammarMap.keySet()); //Get set of all keys
			ArrayList<String> symbolList = (ArrayList<String>) entry.getValue(); //Get rows of symbols

			for (int i = 0; i < symbolList.size(); i++) {
				String temp = symbolList.get(i);

				if(temp.length() == 1) {//If length of temp is 1, it means unit production should be done

					for (int k = 0; k < keySet.size(); k++) {
						if (keySet.get(k).equals(temp)) { //Find key that corresponding to temp

							keyToAddSymbols = entry.getKey().toString();
							List<String> symbolsToAdd = grammarMap.get(temp); //symbols will replace temp
							symbolList.remove(temp); //remove temp because corresponding symbols replace it

							for (int l = 0; l < symbolsToAdd.size(); l++) { // add symbols
								grammarMap.get(keyToAddSymbols).add(symbolsToAdd.get(l));
							}
						}
					}
				}
			}
		}
	}

	private static void eliminateTerminals(ArrayList<String> keyList, int asciiBegin, int count){
		Iterator itr = grammarMap.entrySet().iterator();
		keyList = new ArrayList<>();
		Map<String,List<String>> tempMap = new LinkedHashMap<>();

		Iterator itr2 = grammarMap.entrySet().iterator();
		String newKey = "";
		String replacedKey = "";
		String replacedValue = "";
		boolean noAgain = false;
		boolean noAgain2 = false;


		while (itr.hasNext()) {
			Map.Entry entry = (Map.Entry) itr.next();
			ArrayList<String> productionRow = (ArrayList<String>) entry.getValue();

			keyList.add(entry.getKey().toString());
		}

		while (itr2.hasNext()){
			Map.Entry entry = (Map.Entry) itr2.next();
			ArrayList<String> productionList = (ArrayList<String>) entry.getValue();

			for (int i = 0; i < productionList.size(); i++) {
				String currentItem = productionList.get(i);

				for (int j = 0; j < currentItem.length(); j++) {
					boolean found = false;
					if(!keyList.contains(String.valueOf(currentItem.charAt(j)))){

						List<String> toAdd = new ArrayList<>();

						toAdd.add(String.valueOf(currentItem.charAt(j)));
						newKey = Character.toString((char) asciiBegin);
						asciiBegin++;
						tempMap.put(newKey, toAdd);
						Iterator lastIte = grammarMap.entrySet().iterator();
						keyList.add(newKey);

						while (lastIte.hasNext()){
							Map.Entry iteEntry = (Map.Entry) lastIte.next();
							List<String> iteEntryValues = (List<String>) iteEntry.getValue();
							List<String> tempValues = new ArrayList<>();
							if (iteEntryValues.size() > 1){
								for (int k = 0; k < iteEntryValues.size(); k++) {
									if (iteEntryValues.get(k).contains(String.valueOf(currentItem.charAt(j)))){
										replacedValue = iteEntryValues.get(k);
										replacedValue = replacedValue.replace(String.valueOf(currentItem.charAt(j)), newKey);
										tempValues.add(replacedValue);
									}
									else {
										tempValues.add(iteEntryValues.get(k));
									}
								}
								tempMap.put(iteEntry.getKey().toString(),tempValues);
							}
						}
						keyList.add(String.valueOf(currentItem.charAt(j)));
						noAgain = true;
						break;
					}
				}
				if (noAgain){
					noAgain2 = true;
					break;
				}
			}
			if (noAgain2)
				break;
		}

		grammarMap.putAll(tempMap);
		count++;

		if (count < terminalCount)
			eliminateTerminals(keyList,asciiBegin,count);
		unitProduction();
	}

	public static boolean isKeyFoundInMapValues(Map<String, List<String>> map, String key) {
		for (List<String> values : map.values()) {
			if (values.size() < 2 && values.contains(key)) {
				return false;
			}
		}
		return true;
	}

	public static void variablesLongerThanTwo() {
		Iterator itr = grammarMap.entrySet().iterator();
		String key = "";
		int asciiBegin = 73;
		Map<String, List<String>> tempList = new LinkedHashMap<>();
		ArrayList<String> keyList = new ArrayList<>();


		while (itr.hasNext()) {

			Map.Entry entry = (Map.Entry) itr.next();
			ArrayList<String> keySet = new ArrayList<String>(grammarMap.keySet());
			ArrayList<String> productionList = (ArrayList<String>) entry.getValue();
			Boolean found = false;
			Boolean found1 = false;

			for (int i = 0; i < productionList.size(); i++) {
				String temp = productionList.get(i);

				for (int j = 0; j < temp.length(); j++) {

					if (temp.length() == 3) {
						String newProduction = temp.substring(1, 3);

						if(isKeyFoundInMapValues(tempList, newProduction) && isKeyFoundInMapValues(grammarMap, newProduction)) {
							found = true;
						}
						else found = false;

						if (found) {

							ArrayList<String> newVariable = new ArrayList<>();
							newVariable.add(newProduction);
							key = Character.toString((char) asciiBegin);

							tempList.put(key, newVariable);
							asciiBegin++;
						}

					}

					else if (temp.length() >= 4) {

						String newProduction1 = temp.substring(0, 2); // SA
						String newProduction2 = temp.substring(2, temp.length()); // SA

						if (isKeyFoundInMapValues(tempList, newProduction1) && isKeyFoundInMapValues(grammarMap, newProduction1)) {
							found = true;
						} else {
							found = false;
						}

						if (isKeyFoundInMapValues(tempList, newProduction2) && isKeyFoundInMapValues(grammarMap, newProduction2)) {
							found1 = true;
						} else {
							found1 = false;
						}


						if (found) {

							ArrayList<String> newVariable = new ArrayList<>();
							newVariable.add(newProduction1);
							key = Character.toString((char) asciiBegin);

							tempList.put(key, newVariable);
							asciiBegin++;
						}

						if (found1) {
							ArrayList<String> newVariable = new ArrayList<>();
							newVariable.add(newProduction2);
							key = Character.toString((char) asciiBegin);

							tempList.put(key, newVariable);
							asciiBegin++;
						}
					}

				}

			}


		}
		grammarMap.putAll(tempList);

		itr = grammarMap.entrySet().iterator();

		// obtain key that use to eliminate two variable and above
		while (itr.hasNext()) {
			Map.Entry entry = (Map.Entry) itr.next();
			ArrayList<String> productionList = (ArrayList<String>) entry.getValue();

			if (productionList.size() < 2) {
				keyList.add(entry.getKey().toString());
			}
		}

		itr = grammarMap.entrySet().iterator();
		while (itr.hasNext()) {
			Map.Entry entry = (Map.Entry) itr.next();
			ArrayList<String> productionList = (ArrayList<String>) entry.getValue();

			if (productionList.size() > 1) {
				for (int i = 0; i < productionList.size(); i++) {
					String temp = productionList.get(i);

					for (int j = 0; j < temp.length(); j++) {

						if (temp.length() > 2) {
							String stringToBeReplaced1 = temp.substring(temp.length() - j, temp.length());
							String stringToBeReplaced2 = temp.substring(0, temp.length() - j);

							for (String key2 : keyList) {

								List<String> keyValues = new ArrayList<>();
								keyValues = grammarMap.get(key2);
								String[] values = keyValues.toArray(new String[keyValues.size()]);
								String value = values[0];

								if (stringToBeReplaced1.equals(value)) {

									grammarMap.get(entry.getKey().toString()).remove(temp);
									temp = temp.replace(stringToBeReplaced1, key2);

									if (!grammarMap.get(entry.getKey().toString()).contains(temp)) {
										grammarMap.get(entry.getKey().toString()).add(i, temp);
									}
								} else if (stringToBeReplaced2.equals(value)) {

									grammarMap.get(entry.getKey().toString()).remove(temp);
									temp = temp.replace(stringToBeReplaced2, key2);

									if (!grammarMap.get(entry.getKey().toString()).contains(temp)) {
										grammarMap.get(entry.getKey().toString()).add(i, temp);
									}
								}
							}
						}

					}
				}
			}
		}
	}


	public static void main(String[] args) {
		readToString();
		System.out.println("Original Grammar: \n");
		printMap();
		System.out.println("Remove Epsilon\n");
		for (int i = 0; i < lineCount-1; i++) { // For every row except first one
			removeEpsilon();
			printMap();
		}
		System.out.println("Unit Production\n");
		for (int i = 0; i < lineCount; i++) {// For every row
			unitProduction();
			printMap();
		}
		System.out.println("Eliminate Terminals\n");
		int asciiBegin = 70; //F


		// Terminal count and identifying where to stop eliminateTerminal() func.
		Iterator ite2 = grammarMap.entrySet().iterator();
		ArrayList<String> keyList = new ArrayList<>();
		while (ite2.hasNext()){
			Map.Entry iteEntry = (Map.Entry) ite2.next();
			keyList.add(iteEntry.getKey().toString());
		}
		ArrayList<String> terminal = new ArrayList<>();
		Iterator ite = grammarMap.entrySet().iterator();
		while (ite.hasNext()){
			Map.Entry iteEntry = (Map.Entry) ite.next();
			List<String> iteEntryValues = (List<String>) iteEntry.getValue();
			for (int i = 0; i < iteEntryValues.size(); i++) {
				String current = iteEntryValues.get(i);
				for (int j = 0; j < current.length(); j++) {
					if (!keyList.contains(String.valueOf(current.charAt(j))) && !terminal.contains(String.valueOf(current.charAt(j)))){
						terminal.add(String.valueOf(current.charAt(j)));
					}
				}
			}
		}
		terminalCount = terminal.size();
		int count = 0;
		ArrayList<String> exList = new ArrayList<>();
		eliminateTerminals(exList,asciiBegin,count);
		printMap();

		System.out.println("Break Variable Strings Longer Than 2:\n");
		variablesLongerThanTwo();
		printMap();

	}

}

