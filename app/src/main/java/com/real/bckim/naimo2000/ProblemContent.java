package com.real.bckim.naimo2000;

/**
 * Created by bckim on 2018-03-27.
 */

public class ProblemContent{
    int id;

    String DB_Name;
    int ID_word;

    int[] pair = new int[2];

    boolean imageExist;
    boolean text1Exist;
    boolean text2Exist;

    String text1;
    String text2;

    public ProblemContent(){}
    public int getID(){return this.id;}
    public void setID(int id){this.id=id;}
    public String getDB_Name(){return this.DB_Name;}
    public void setDB_Name(String DB_Name){this.DB_Name=DB_Name;}
    public int getID_word(){return this.ID_word;}
    public void setID_word(int ID_word){this.ID_word=ID_word;}
    public int[] getPair(){return this.pair;}
    public void setPair(){
        int numItem=0;
        if(this.imageExist){numItem+=1;}
        if(this.text1Exist){numItem+=1;}
        if(this.text2Exist){numItem+=1;}
        switch(numItem) {
            case 2:
                if (!this.imageExist) {
                    pair[0] = 2;
                    pair[1] = 3;
                }
                if (!this.text1Exist) {
                    pair[0] = 1;
                    pair[1] = 3;
                }
                if (!this.text2Exist) {
                    pair[0] = 1;
                    pair[1] = 2;
                }
                break;
            case 3:
                pair = GameActivity_PairSelection.getRandomSeries(2, 2);
                pair[0]+=1; pair[1]+=1;
                break;
        }
    }
    public boolean getImageExist(){return this.imageExist;}
    public void setImageExist(boolean imageExist){this.imageExist=imageExist;}
    public boolean getText1Exist(){return this.text1Exist;}
    public void setText1Exist(boolean text1Exist){this.text1Exist=text1Exist;}
    public boolean getText2Exist(){return this.text2Exist;}
    public void setText2Exist(boolean text2Exist){this.text2Exist=text2Exist;}
    public String getText1(){return this.text1;}
    public void setText1(String text1){this.text1=text1;}
    public String getText2(){return this.text2;}
    public void setText2(String text2){this.text2=text2;}
}