package com.Analysis;

import com.database.ResultOperator;
import com.generate.GenerateLibrary;
import com.state.DatePattern;
import com.state.KeyboardState;
import com.strength.Estimation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lgluo on 2016/10/14.
 */
public class Analyse {

    private ResultOperator operator = new ResultOperator();

    public void characterDistribution(String password){
        Map<Character, Integer> characterMap = new HashMap<>();
        for(int i = 0; i < password.length(); i++) {
            char temp = password.charAt(i);
            if(characterMap.get(temp) == null) {
                int num = characterMap.get(temp) == null ? 1 : characterMap.get(temp)+1;
                characterMap.put(temp, num);
            } else {
                characterMap.put(temp,  characterMap.get(temp)+1);
            }
        }
        operator.analyseCharacter(characterMap);
    }

    public void structures(String password){
        String structure = "";
        for(int temp = 0; temp < password.length(); temp++) {
            char i = password.charAt(temp);
            if(i >= 48 && i <= 57) {
                structure += "D";
            } else if(i >= 65 && i <= 90) {
                structure += "U";
            } else if(i >= 97 && i <= 122) {
                structure += "L";
            } else {
                structure += "S";
            }
        }
        operator.add(structure, "structure");
    }

    public void keyboardPattern(String password){
        KeyboardState result = KeyboardState.NO_PATTERN;
        boolean sameRow = true;
        boolean zigZag = true;
        for(int i = 1; i < password.length(); i++) {
            char pos1 = password.charAt(i-1);
            char pos2 = password.charAt(i);
            if(KeyboardClass.getInstance().isAdjacent(pos1, pos2)) {
                sameRow = sameRow & KeyboardClass.getInstance().isSameRow(pos1, pos2);
                zigZag = zigZag & !KeyboardClass.getInstance().isSameRow(pos1,pos2);
            } else {
                operator.addKeyboardPattern(result.getState());
                return;
            }
        }
        if(sameRow) {
            if(KeyboardClass.getInstance().getPosition(password.charAt(0)).get(1) == 0) {
                result = KeyboardState.SAME_ROW_NUMBER_ONLY;
            } else {
                result = KeyboardState.SAME_ROW;
            }
        } else if(zigZag) {
            result = KeyboardState.ZIG_ZAG;
        } else {
            result = KeyboardState.SNAKE;
        }
        operator.addKeyboardPattern(result.getState());
    }

    public void wordsAnalyse(String password) {
        //convert the input to letter-only
        boolean isUppercase = false;
        for(int i = 0; i < password.length(); i++) {
            if(password.charAt(i) >= 65 && password.charAt(i) <= 90) {
                isUppercase = true;
            }
        }
        String input = "";
        boolean letterOnly = true;
        password = password.toLowerCase();
        for(int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            if(c >= 97 && c <= 122) {
                input += c;
            } else {
                letterOnly = false;
            }
        }
        boolean isPinyin = WordsMatch.getInstance().identifyWord(input, "pinyin");
        boolean isEnglish = WordsMatch.getInstance().identifyWord(input, "english");
        operator.addWordsPattern(isPinyin, isEnglish, letterOnly, input, isUppercase);
    }

    public void dataFormat(String password) {
        //find the consecutive numbers of exactly six or eight digits
        String date = "";
        int result = 0;
        DatePattern datePattern = DatePattern.DIGIT_ONLY;
        for(int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            if((c >= 65 && c <= 90) || (c >= 97 && c <= 122)) {
                if(datePattern == DatePattern.DIGIT_ONLY) {
                    datePattern = DatePattern.LETTER_DIGIT;
                } else if(datePattern == DatePattern.SYMBOL_DIGIT) {
                    datePattern = DatePattern.LETTER_DIGIT_SYMBOL;
                }
            } else if(c < 48 || c > 57) {
                if(datePattern == DatePattern.DIGIT_ONLY) {
                    datePattern = DatePattern.SYMBOL_DIGIT;
                } else if(datePattern == DatePattern.LETTER_DIGIT) {
                    datePattern = DatePattern.LETTER_DIGIT_SYMBOL;
                }
            }
        }
        for(int i = 0; i < password.length(); i++) {
            String temp = "";
            int j = i;
            for(; j < password.length(); j++) {
                char tempc = password.charAt(j);
                if(tempc < 48 || tempc > 57) {
                    break;
                }
                temp += tempc;
            }
            if(temp.length() == 6 || temp.length() == 8) {
                date = temp;
                break;
            } else {
                i = j;
            }
        }
        if(date.length() == 6 || date.length() == 8) {
            result = DateFormat.getInstance().dateAnalyse(date);
            if(result != 7) {
                operator.addDatePattern(result, datePattern.getDatePattern());
            } else {
                operator.addDatePattern(datePattern.getDatePattern());
            }
        }
    }

    public void analyse(String filename) {
        File file = new File(filename);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            while((tempString = reader.readLine()) != null) {
                operator.add(tempString, "password");
                characterDistribution(tempString);
                structures(tempString);
                keyboardPattern(tempString);
                wordsAnalyse(tempString);
                dataFormat(tempString);
                //Estimation::add() is a training process
//                Estimation.getInstance().add(tempString);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        operator.addRestDatePattern(DateFormat.getInstance().getRestDatePattern());
        operator.output(filename);
    }


//    public static void main(String args[]){
//        Analyse analyse = new Analyse();
//        File file = new File("test");
//        BufferedReader reader = null;
//        try {
//            reader = new BufferedReader(new FileReader(file));
//            String tempString = null;
//            while((tempString = reader.readLine()) != null) {
//               ResultOperator.getInstance().add(tempString, "password");
//                analyse.characterDistribution(tempString);
//                analyse.structures(tempString);
//                analyse.keyboardPattern(tempString);
//                analyse.wordsAnalyse(tempString);
//                analyse.dataFormat(tempString);
//                //Estimation::add() is a training process
////                Estimation.getInstance().add(tempString);
//            }
//            reader.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        ResultOperator.getInstance().addRestDatePattern(DateFormat.getInstance().getRestDatePattern());
//        ResultOperator.getInstance().output();
//        //GenerateLibrary::generatePassword() can't be invoked before ResultOperator::output()
//        //because GenerateLibrary should use result.log to initialization
//        GenerateLibrary.getInstance().generatePassword();
//    }
}
