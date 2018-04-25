package com.real.bckim.naimo2000;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import static android.graphics.Paint.ANTI_ALIAS_FLAG;
import static com.real.bckim.naimo2000.MainActivity.REQ_CODE_SELECT_IMAGE;

public class GameActivity_PairSelection extends AppCompatActivity {
    String DB_Name;
    String Word_DB_Name;
    ListDataHandler_Word db_word;

    ImageView[][] btnSet;
    LinearLayout[] ColLayerSet;
    LinearLayout[][] LinLayerSet;

    int quizArraySize_Row;
    int quizArraySize_Col;
    int numButton;
    int numQuiz;
    ImageView[] btnQuiz;
    ArrayList<ProblemContent> ProblemList= new ArrayList<>();

    int[] btnArray;  //버튼섞는 난수배열
    int[] problemArray;  //문제출제용 난수배열
    int[] btnSolutionArray; //정답인지 아닌지 확인용
    int lastSelectedButtonID;
    int remainedQuiz;
    Bitmap image_bitmap; //메모리 절약을 위해 비트맵은 Activity마다 한개만 쓰면 좋을 듯.

    int btnWidth;
    int btnHeight;
    int numMaxCharOfBtn;
    int numMaxLineOfBtn;

    WordEditingDialog wordEditingDialog;
    int SelectedID;

    String[] btnSpeakTexts; //버튼 클릭 시 읽어줄 텍스트 설정
    TextToSpeech tts;
    String speachText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pair_game);

        Configuration();

        ObjectMatching();
        HideUndefinedButtons();
        QuizButtonMatching();

        ButtonClickListenerSetting();
        getButtonSize();

        if(BuildProblemSet()){
            MakeQuiz();
            remainedQuiz = numQuiz;
        }else{
            finish();
        }
    }
    private void getButtonSize(){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;
        btnWidth = screenWidth/quizArraySize_Col;
        btnHeight = screenHeight/quizArraySize_Row;

        //버튼에 들어가는 글자 줄 수 및 한줄의 글자 개수 설정
        int textHeight = getResources().getInteger(R.integer.pairGameTextSize);
        int textWidth = (int) (textHeight * 0.6f);
        numMaxLineOfBtn = (int) (btnHeight*1.05) / textHeight;
        if(numMaxLineOfBtn <1) numMaxLineOfBtn =1;
        numMaxCharOfBtn = btnWidth / textWidth;
        if(numMaxCharOfBtn <1) numMaxCharOfBtn =1;
    }
    private void Configuration(){
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN); //풀스크린

        Intent intent = getIntent();
        DB_Name = intent.getStringExtra("DB_Name");
        int bookID = intent.getIntExtra("bookID",0);
        Word_DB_Name = WordListActivity.pre_Word_DB_Name + bookID;
        //db_word = new ListDataHandler_Word(this, Word_DB_Name);
        db_word = WordListActivity.db_word;

        quizArraySize_Row = intent.getIntExtra("QuizArraySize_Row",getResources().getInteger(R.integer.defaultQuizArraySize_Row));
        quizArraySize_Col = intent.getIntExtra("QuizArraySize_Col",getResources().getInteger(R.integer.defaultQuizArraySize_Column));

        numButton= quizArraySize_Row * quizArraySize_Col;
        numQuiz=numButton/2;

        ConstraintLayout clBackground= findViewById(R.id.conLayout_background);
        clBackground.setBackground(getResources().getDrawable(R.drawable.quiz_background));

    }
    private void speakBtnText(int btnID){
        speachText = btnSpeakTexts[btnID];
        tts=new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS){
                    int result=tts.setLanguage(Locale.US);
                    if(result==TextToSpeech.LANG_MISSING_DATA ||
                            result==TextToSpeech.LANG_NOT_SUPPORTED){
                    }
                    else{
                        ConvertTextToSpeech();
                    }
                }
            }
        });
    }
    private void ConvertTextToSpeech() {
        tts.speak(speachText, TextToSpeech.QUEUE_FLUSH, null);
    }
    private void ButtonClickEventHandler(int btnID){
        speakBtnText(btnID);

        if(lastSelectedButtonID== btnID){
            //같은 버튼은 두번 누른 경우 무시
            return;
        }  else if(lastSelectedButtonID == -1) {
            //처음 누른 버튼인 경우
            lastSelectedButtonID = btnID;
            return;
        }else if(btnSolutionArray[btnID] == btnSolutionArray[lastSelectedButtonID]) {
            //정답인 경우
            if(db_word.getContent(getIdFromBtnId(btnID))!=null) AnswerCase_Correct(btnID);
            btnQuiz[btnID].setVisibility(View.INVISIBLE);
            btnQuiz[lastSelectedButtonID].setVisibility(View.INVISIBLE);
            lastSelectedButtonID = -1;
            remainedQuiz-=1;
            if(remainedQuiz<1) recreate();
            return;
        } else {
            //틀린 경우
            if(db_word.getContent(getIdFromBtnId(lastSelectedButtonID))!=null) AnswerCase_Wrong(lastSelectedButtonID);
            lastSelectedButtonID= btnID;
        }
    }
    private void ButtonLongClickEventHandler(int btnID){
        SelectedID = getIdFromBtnId(btnID);
        Content_Word cw = db_word.getContent(SelectedID);
        wordEditingDialog = new WordEditingDialog(this,db_word,cw);
        wordEditingDialog.showDialog();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        //Toast.makeText(getBaseContext(), "resultCode : "+resultCode,Toast.LENGTH_SHORT).show();
        if(requestCode == REQ_CODE_SELECT_IMAGE) {
            if(resultCode== Activity.RESULT_OK) {
                String imageFilePathName = Manager_TxtMathTools.getPathNameFromURI(getApplicationContext(),intent.getData());
                boolean hasImage = Manager_PreviewImage.setPreviewImage(imageFilePathName,SelectedID);
                Content_Word cw = db_word.getContent(SelectedID);
                cw.setHasImage(hasImage);
                wordEditingDialog = new WordEditingDialog(this,db_word,cw);
                wordEditingDialog.showDialog();
            }
        }
    }
    private void ButtonLongClickEventHandler_old(int btnID){
        final int SelectedID = getIdFromBtnId(btnID);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        //View view = inflater.inflate(R.layout.dialog_word_edit_pair_selection, null);
        View view = inflater.inflate(R.layout.dialog_word_edit, null);
        builder.setView(view);

        final ImageView icon = view.findViewById(R.id.itemimageIcon);
        final EditText editText1 = view.findViewById(R.id.edittextItemName);
        final EditText editText2 = view.findViewById(R.id.edittextItemMemo);
        final EditText editText3 = view.findViewById(R.id.edittextItemMemo2);
        final EditText editText4 = view.findViewById(R.id.edittextItem_Pronunciation);
        ImageButton btnGetDaumPronunciation = view.findViewById(R.id.getDaumPronunciation);
        Button btnModify = view.findViewById(R.id.buttonSubmit_word);
        ImageButton btnDelete = view.findViewById(R.id.buttonDelete_word);

        btnGetDaumPronunciation.setVisibility(View.GONE);

        Content_Word cn = db_word.getContent(SelectedID);
        Manager_PreviewImage.setDataBaseName(Word_DB_Name);
        btnModify.setText(R.string.modify);
        btnDelete.setVisibility(View.INVISIBLE);
        image_bitmap= Manager_PreviewImage.getPreviewImage(SelectedID);
        if(image_bitmap==null){
            icon.setImageResource(R.drawable.plus3);
        }else{
            icon.setImageBitmap(image_bitmap);
        }
        editText1.setText(cn.getText1());
        editText2.setText(cn.getText2());
        editText3.setText(cn.getText3());
        editText4.setText(cn.getText4());

        final AlertDialog dialog_word_edit_pairSelection = builder.create();

        btnModify.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                boolean textChanged= true;
                Content_Word cn = db_word.getContent(SelectedID);
                String text1 = editText1.getText().toString();
                String text2 = editText2.getText().toString();
                String text3 = editText3.getText().toString();
                String text4 = editText4.getText().toString();
                if(text1.equals(cn.getText1())&&text2.equals(cn.getText2())) textChanged=false;
                if(text1.length() != 0){
                    cn.setText1(text1);
                    cn.setText2(text2);
                    cn.setText3(text3);
                    cn.setText4(text4);
                    db_word.updateContent(cn);
                }
                dialog_word_edit_pairSelection.dismiss();
                if(textChanged) Toast.makeText(v.getContext(), R.string.theWordIsModified,Toast.LENGTH_SHORT).show();
            }
        });
        dialog_word_edit_pairSelection.show();
    }
    private void QuizButtonMatching(){
        btnQuiz = new ImageView[numButton];

        int n=0;
        for(int i = 0; i< quizArraySize_Row; i++){
            for(int j = 0; j< quizArraySize_Col; j++){
                btnQuiz[n] = btnSet[i][j];
                n+=1;
            }
        }
    }
    private void HideUndefinedButtons(){
        for(int j = quizArraySize_Col; j<10; j++){
            ColLayerSet[j].setVisibility(View.GONE);
        }
        for(int i = quizArraySize_Row; i<10; i++){
            for(int j=0;j<10;j++){
                btnSet[i][j].setVisibility(View.GONE);
                LinLayerSet[i][j].setVisibility(View.GONE);
            }
        }
    }
    private void MakeQuiz(){
        ImageView btn1;
        ImageView btn2;
        btnSolutionArray = new int[numButton];
        btnSpeakTexts = new String[numButton];

        int nBtn1;
        int nBtn2;
        for(int i = 0; i< numQuiz; i++){
            nBtn1 = btnArray[i*2];
            nBtn2 = btnArray[i*2+1];

            btn1 = btnQuiz[nBtn1];
            btn2 = btnQuiz[nBtn2];

//            ProblemContent pc = ProblemList.get(1);
//            String tmp1 = pc.text1;
//            String tmp2 = pc.text2;
//            pc = ProblemList.get(2);
//            tmp1 = pc.text1;
//            tmp2 = pc.text2;
            ProblemContent pc = ProblemList.get(i);
            boolean tmp3 = pc.getImageExist();
            String tmp1 = pc.getText1();
            String tmp2 = pc.getText2();

            btnSolutionArray[nBtn1]=pc.getID_word();
            btnSolutionArray[nBtn2]=pc.getID_word();

            UpdateWordExposedTime(pc.getID_word());

            btnSetting(btn1,btn2,pc,nBtn1,nBtn2);
        }
    }
    private void UpdateWordExposedTime(int id){
        Content_Word cw = db_word.getContent(id);
        cw.setTimeExposeNow();
        db_word.updateContent(cw);
    }
    private void btnSetting(ImageView btn1, ImageView btn2, ProblemContent pc,int nBtn1, int nBtn2){
        int[] pair;

        ImageView[] bp = new ImageView[2];
        bp[0] = btn1;
        bp[1] = btn2;

        int[] nBtn = new int[2];
        nBtn[0] = nBtn1;
        nBtn[1] = nBtn2;

        Manager_PreviewImage.setDataBaseName(DB_Name);
        int txtSize = getResources().getInteger(R.integer.pairGameTextSize);
        int txtColor = getResources().getColor(R.color.pairGameText);

        for(int i=0;i<2;i++){
            pair = pc.getPair();
            String text1 = pc.getText1();
            String text2 = pc.getText2();

            switch(pair[i]){
                case 1:
                    image_bitmap = Manager_PreviewImage.getPreviewImage(pc.getID_word());
                    bp[i].setScaleType(ImageView.ScaleType.CENTER_CROP);
                    btnSpeakTexts[nBtn[i]]="";
                    break;
                case 2:
                    image_bitmap = textAsBitmap(text1, txtSize, txtColor, numMaxLineOfBtn, numMaxCharOfBtn);
                    bp[i].setScaleType(ImageView.ScaleType.CENTER);
                    //bp[i].setBackground(getResources().getDrawable(R.drawable.btn_gray_buty));
                    //bp[i].setBackgroundColor(0x66FFFFFF);
                    bp[i].setBackgroundColor(getResources().getColor(R.color.pairGameButton1));
                    btnSpeakTexts[nBtn[i]]=text1;
                    break;
                case 3:
                    image_bitmap = textAsBitmap(text2, txtSize, txtColor, numMaxLineOfBtn, numMaxCharOfBtn);
                    bp[i].setScaleType(ImageView.ScaleType.CENTER);
                    bp[i].setBackgroundColor(getResources().getColor(R.color.pairGameButton2));
                    btnSpeakTexts[nBtn[i]]="";
                    break;
            }
            //image_bitmap = setBitmapScale(image_bitmap,2);
            bp[i].setImageBitmap(image_bitmap);
            //bp[i].setText(null);
        }
    }
    private boolean BuildProblemSet(){
        ListDataHandler_Word db_word;
        List<Content_Word> contents;
        db_word = new ListDataHandler_Word(this, DB_Name);
        Manager_PreviewImage.setDataBaseName(DB_Name);
        contents = db_word.getQuizContents();
        //int numContent = db_word.getCount();
        int numContent = contents.size();

        if(numContent<1){
            Toast.makeText(this,"퀴즈를 만들 수 있는 단어가 하나도 없습니다.",Toast.LENGTH_SHORT).show();
            return false;
        }

        btnArray = getRandomSeries(numButton-1,numButton);
        problemArray = getRandomSeries(numContent-1, numQuiz);

        int listIndex = 0;
        for(int i = 0; i< numQuiz; i++) {
            Content_Word cn = contents.get(problemArray[i]);
            ProblemContent pc = new ProblemContent();

            pc.setDB_Name(DB_Name);
            pc.setID_word(cn.getID());
            String text1 = cn.getText1();
            String text2 = cn.getText2();
            pc.setText1(text1);
            pc.setText2(text2);

            //pc.setImageExist(Manager_PreviewImage.exists(pc.getID_word()));
            pc.setImageExist(cn.getHasImage());
            pc.setText1Exist(false); pc.setText2Exist(false);
            if(text1.length()>0 && !text1.equals(" ")){pc.setText1Exist(true);}
            if(text2.length()>0 && !text2.equals(" ")){pc.setText2Exist(true);}

            pc.setPair();
            ProblemList.add(pc);
        }
        //numProblem=ProblemList.size()+1;

//        ProblemContent cw = ProblemList.get(1);
//        String tmp1 = cw.getDB_Name();
//        int tmp2 =  cw.getID_word();
//        String tmp3 = cw.getText1();
//        String tmp4 = cw.getText2();
//
//        cw = ProblemList.get(2);
//        String tmp5 = cw.getDB_Name();
//        int tmp6 =  cw.getID_word();
//        String tmp7 = cw.getText1();
//        String tmp8 = cw.getText2();
        return true;
    }
    private void ButtonClickListenerSetting(){
        lastSelectedButtonID=-1;
        for(int i=0;i<numButton;i++){
            final int clickedButtonID = i;
            btnQuiz[i].setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    ButtonClickEventHandler(clickedButtonID);
                }
            });
            btnQuiz[i].setOnLongClickListener(new View.OnLongClickListener(){
                public boolean onLongClick(View v){
                    ButtonLongClickEventHandler(clickedButtonID);
                    return false;
                }
            });
        }
    }
    private int getIdFromBtnId(int btnID){
        int word_ID = btnSolutionArray[btnID];
        return word_ID;
    }
    private void AnswerCase_Correct(int btnID){
        int id = getIdFromBtnId(btnID);
        Content_Word cw = db_word.getContent(id);
        cw.setNumCorrect(cw.getNumCorrect()+1);
        cw.setFamiliarity(cw.getFamiliarity()+5);
        db_word.updateContent(cw);
    }
    private void AnswerCase_Wrong(int btnID1){
        Double decreaseRatio = 0.85;

        int id1 = getIdFromBtnId(btnID1);
        Content_Word cw = db_word.getContent(id1);
        cw.setNumWrong(cw.getNumWrong()+1);
        Double familiarity = (double) cw.getFamiliarity();
        familiarity = familiarity * decreaseRatio;
        cw.setFamiliarity(familiarity.intValue());
        db_word.updateContent(cw);
    }
    private void ButtonClickEventHandler_old(int id){
        //Toast.makeText(this,id+" " ,Toast.LENGTH_SHORT).show();
        if(lastSelectedButtonID==id || lastSelectedButtonID == -1){
            lastSelectedButtonID=id;
            return;
        }else if(btnSolutionArray[id] == btnSolutionArray[lastSelectedButtonID]){
            //btnQuiz[id].setEnabled(false);
            //btnQuiz[lastSelectedButtonID].setEnabled(false);
            btnQuiz[id].setVisibility(View.INVISIBLE);
            btnQuiz[lastSelectedButtonID].setVisibility(View.INVISIBLE);
            remainedQuiz-=1;
            if(remainedQuiz<2) recreate();
        }
        lastSelectedButtonID=id;
    }
    private void getPairID(int id){

    }
    public static int getRandomNumber(int MaxNumber){
        //0~MaxNumber 까지 랜덤선택
        Random random = new Random();
        return random.nextInt(MaxNumber+1);
    }
    public static int[] getRandomSeries(int MaxNumber,int numNumbers){
        //0~MaxNumber 중에서 중복되지 않는 numNumbers개의 난수 배열 생성
        //numNumbers > MaxNumber+1 인 경우 중복허용
        //ref http://blog.danggun.net/791
        int[] RS = new int[numNumbers];
        int[] list = new int[MaxNumber+1];
        int select;
        if(numNumbers>MaxNumber+1){
            for(int i=0;i<numNumbers;i++){
                RS[i]=getRandomNumber(MaxNumber);
            }
        }else{
            for(int i=0;i<MaxNumber+1;i++){list[i]=i;}
            for(int i=0;i<numNumbers;i++){
                select=getRandomNumber(MaxNumber-i);
                RS[i] = list[select];
                list[select]=list[MaxNumber-i];
            }
        }
        return RS;
    }
    private int[] getNTxtLineAndMaxLength_old(int btnWidth, int btnHeight){
        int textWidth = 30;

        int nMaxLine;
        int nMaxChar;
        int[] rltInts = new int[2];

        float wRatio = (float) btnWidth / (float) btnHeight;
        nMaxChar = btnWidth / textWidth;
        if(nMaxChar<4) nMaxChar=4;
        if(wRatio<0.8) wRatio=1/wRatio;

        nMaxLine=4;
        if(wRatio>3) nMaxLine=3;
        if(wRatio>6) nMaxLine=2;
        if(wRatio>10) nMaxLine=1;

        rltInts[0] = nMaxLine;
        rltInts[1] = nMaxChar;
        return rltInts;
    }
//    private String[] SplitMultiRegularExpression(String text){
//
//        int strIdx = 0;
//        int endIdx = 0;
//        int[] strIdxs = new int[text.length()/2+1];
//        int[] endIdxs = new int[text.length()/2+1];
//        int wordLength = 0;
//        int numWord =0;
//
//        //int[] idxSet = new int[wordLength];
//
//        int orderWord = 0;
//        for(int i=0;i<text.length();i++){
//            if(isRegEx(text.substring(endIdx,endIdx))){
//                if(wordLength>0){
//                    numWord+=1;
//                    wordLength=0;
//                    endIdxs[numWord]=endIdx;
//                }
//            }else{
//                wordLength+=1;
//                if(wordLength==1){
//                    if(numWord==0){
//                        numWord=1;
//                    }
//                    orderWord+=1;
//                    strIdxs[numWord]=endIdx;
//                }
//            }
//        }
//        endIdxs[numWord-1]=text.length();
//
//        String[] texts = new String[numWord];
//        String[] regs = new String[numWord];
//
//        for(int i=0;i<numWord;i++){
//           texts[i]=text.substring(strIdxs[i],endIdxs[i]);
//        }
//        return texts;
//    }
    public Bitmap textAsBitmap(String text, float textSize, int textColor, int nMaxLine, int nMaxByte) {
        String[] mTexts = Manager_TxtMathTools.DivideStringMultiline(text,nMaxByte,nMaxLine);
        int numLine = mTexts.length;
        if(numLine>nMaxLine) numLine=nMaxLine;

        Paint paint = new Paint(ANTI_ALIAS_FLAG);
        paint.setTextSize(textSize);
        paint.setColor(textColor);
        paint.setTextAlign(Paint.Align.LEFT);

        float baseline = -paint.ascent(); // ascent() is negative
        float paintDescent = paint.descent();
        int height = (int) (baseline + paintDescent + 0.5f);
        height += textSize*(numLine-1);

        int maxWidth =0;
        int[] widths = new int[numLine];
        for(int i=0;i<numLine;i++){
            widths[i] = (int) (paint.measureText(mTexts[i]) + 0.5f);
            if(widths[i]> maxWidth) maxWidth = widths[i];
        }

        Bitmap image = Bitmap.createBitmap(maxWidth, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);
        //canvas.drawColor(Color.YELLOW);

        for(int i=0;i<numLine;i++){

            float x = 0;
            float y = baseline+i*textSize;
            canvas.drawText(mTexts[i], x+(maxWidth-widths[i])/2, y, paint);
        }

//        canvas.drawText("This is", 100, 100, mTextPaint);
//        canvas.drawText("multi-line", 100, 150, mTextPaint);
//        canvas.drawText("text", 100, 200, mTextPaint);
        return image;
    }
    public Bitmap textAsBitmap_old(String text, float textSize, int textColor) {
        Paint paint = new Paint(ANTI_ALIAS_FLAG);
        paint.setTextSize(textSize);
        paint.setColor(textColor);
        paint.setTextAlign(Paint.Align.LEFT);
        float baseline = -paint.ascent(); // ascent() is negative
        int width = (int) (paint.measureText(text) + 0.5f); // round
        int height = (int) (baseline + paint.descent() + 0.5f);
        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);
        //canvas.drawColor(Color.YELLOW);
        canvas.drawText(text, 0, baseline, paint);
        return image;
    }
    public Bitmap setBitmapScale(Bitmap bitmap,float scale){
        //왠진 모르겠으나 안됨.
        int width  = bitmap.getWidth();
        int height = bitmap.getHeight();
        width *= scale;
        height *= scale;
        bitmap.setWidth(width);
        bitmap.setHeight(height);
        return bitmap;
    }
    private void ObjectMatching(){
        btnSet = new ImageView[10][10];
        btnSet[0][0] = findViewById(R.id.btn00);
        btnSet[1][0] = findViewById(R.id.btn10);
        btnSet[2][0] = findViewById(R.id.btn20);
        btnSet[3][0] = findViewById(R.id.btn30);
        btnSet[4][0] = findViewById(R.id.btn40);
        btnSet[5][0] = findViewById(R.id.btn50);
        btnSet[6][0] = findViewById(R.id.btn60);
        btnSet[7][0] = findViewById(R.id.btn70);
        btnSet[8][0] = findViewById(R.id.btn80);
        btnSet[9][0] = findViewById(R.id.btn90);

        btnSet[0][1] = findViewById(R.id.btn01);
        btnSet[1][1] = findViewById(R.id.btn11);
        btnSet[2][1] = findViewById(R.id.btn21);
        btnSet[3][1] = findViewById(R.id.btn31);
        btnSet[4][1] = findViewById(R.id.btn41);
        btnSet[5][1] = findViewById(R.id.btn51);
        btnSet[6][1] = findViewById(R.id.btn61);
        btnSet[7][1] = findViewById(R.id.btn71);
        btnSet[8][1] = findViewById(R.id.btn81);
        btnSet[9][1] = findViewById(R.id.btn91);

        btnSet[0][2] = findViewById(R.id.btn02);
        btnSet[1][2] = findViewById(R.id.btn12);
        btnSet[2][2] = findViewById(R.id.btn22);
        btnSet[3][2] = findViewById(R.id.btn32);
        btnSet[4][2] = findViewById(R.id.btn42);
        btnSet[5][2] = findViewById(R.id.btn52);
        btnSet[6][2] = findViewById(R.id.btn62);
        btnSet[7][2] = findViewById(R.id.btn72);
        btnSet[8][2] = findViewById(R.id.btn82);
        btnSet[9][2] = findViewById(R.id.btn92);

        btnSet[0][3] = findViewById(R.id.btn03);
        btnSet[1][3] = findViewById(R.id.btn13);
        btnSet[2][3] = findViewById(R.id.btn23);
        btnSet[3][3] = findViewById(R.id.btn33);
        btnSet[4][3] = findViewById(R.id.btn43);
        btnSet[5][3] = findViewById(R.id.btn53);
        btnSet[6][3] = findViewById(R.id.btn63);
        btnSet[7][3] = findViewById(R.id.btn73);
        btnSet[8][3] = findViewById(R.id.btn83);
        btnSet[9][3] = findViewById(R.id.btn93);

        btnSet[0][4] = findViewById(R.id.btn04);
        btnSet[1][4] = findViewById(R.id.btn14);
        btnSet[2][4] = findViewById(R.id.btn24);
        btnSet[3][4] = findViewById(R.id.btn34);
        btnSet[4][4] = findViewById(R.id.btn44);
        btnSet[5][4] = findViewById(R.id.btn54);
        btnSet[6][4] = findViewById(R.id.btn64);
        btnSet[7][4] = findViewById(R.id.btn74);
        btnSet[8][4] = findViewById(R.id.btn84);
        btnSet[9][4] = findViewById(R.id.btn94);

        btnSet[0][5] = findViewById(R.id.btn05);
        btnSet[1][5] = findViewById(R.id.btn15);
        btnSet[2][5] = findViewById(R.id.btn25);
        btnSet[3][5] = findViewById(R.id.btn35);
        btnSet[4][5] = findViewById(R.id.btn45);
        btnSet[5][5] = findViewById(R.id.btn55);
        btnSet[6][5] = findViewById(R.id.btn65);
        btnSet[7][5] = findViewById(R.id.btn75);
        btnSet[8][5] = findViewById(R.id.btn85);
        btnSet[9][5] = findViewById(R.id.btn95);

        btnSet[0][6] = findViewById(R.id.btn06);
        btnSet[1][6] = findViewById(R.id.btn16);
        btnSet[2][6] = findViewById(R.id.btn26);
        btnSet[3][6] = findViewById(R.id.btn36);
        btnSet[4][6] = findViewById(R.id.btn46);
        btnSet[5][6] = findViewById(R.id.btn56);
        btnSet[6][6] = findViewById(R.id.btn66);
        btnSet[7][6] = findViewById(R.id.btn76);
        btnSet[8][6] = findViewById(R.id.btn86);
        btnSet[9][6] = findViewById(R.id.btn96);

        btnSet[0][7] = findViewById(R.id.btn07);
        btnSet[1][7] = findViewById(R.id.btn17);
        btnSet[2][7] = findViewById(R.id.btn27);
        btnSet[3][7] = findViewById(R.id.btn37);
        btnSet[4][7] = findViewById(R.id.btn47);
        btnSet[5][7] = findViewById(R.id.btn57);
        btnSet[6][7] = findViewById(R.id.btn67);
        btnSet[7][7] = findViewById(R.id.btn77);
        btnSet[8][7] = findViewById(R.id.btn87);
        btnSet[9][7] = findViewById(R.id.btn97);

        btnSet[0][8] = findViewById(R.id.btn08);
        btnSet[1][8] = findViewById(R.id.btn18);
        btnSet[2][8] = findViewById(R.id.btn28);
        btnSet[3][8] = findViewById(R.id.btn38);
        btnSet[4][8] = findViewById(R.id.btn48);
        btnSet[5][8] = findViewById(R.id.btn58);
        btnSet[6][8] = findViewById(R.id.btn68);
        btnSet[7][8] = findViewById(R.id.btn78);
        btnSet[8][8] = findViewById(R.id.btn88);
        btnSet[9][8] = findViewById(R.id.btn98);

        btnSet[0][9] = findViewById(R.id.btn09);
        btnSet[1][9] = findViewById(R.id.btn19);
        btnSet[2][9] = findViewById(R.id.btn29);
        btnSet[3][9] = findViewById(R.id.btn39);
        btnSet[4][9] = findViewById(R.id.btn49);
        btnSet[5][9] = findViewById(R.id.btn59);
        btnSet[6][9] = findViewById(R.id.btn69);
        btnSet[7][9] = findViewById(R.id.btn79);
        btnSet[8][9] = findViewById(R.id.btn89);
        btnSet[9][9] = findViewById(R.id.btn99);

        ColLayerSet = new LinearLayout[10];
        ColLayerSet[0] = findViewById(R.id.LinLay_C0);
        ColLayerSet[1] = findViewById(R.id.LinLay_C1);
        ColLayerSet[2] = findViewById(R.id.LinLay_C2);
        ColLayerSet[3] = findViewById(R.id.LinLay_C3);
        ColLayerSet[4] = findViewById(R.id.LinLay_C4);
        ColLayerSet[5] = findViewById(R.id.LinLay_C5);
        ColLayerSet[6] = findViewById(R.id.LinLay_C6);
        ColLayerSet[7] = findViewById(R.id.LinLay_C7);
        ColLayerSet[8] = findViewById(R.id.LinLay_C8);
        ColLayerSet[9] = findViewById(R.id.LinLay_C9);

        LinLayerSet = new LinearLayout[10][10];
        LinLayerSet[0][0] = findViewById(R.id.LinLay_00);
        LinLayerSet[1][0] = findViewById(R.id.LinLay_10);
        LinLayerSet[2][0] = findViewById(R.id.LinLay_20);
        LinLayerSet[3][0] = findViewById(R.id.LinLay_30);
        LinLayerSet[4][0] = findViewById(R.id.LinLay_40);
        LinLayerSet[5][0] = findViewById(R.id.LinLay_50);
        LinLayerSet[6][0] = findViewById(R.id.LinLay_60);
        LinLayerSet[7][0] = findViewById(R.id.LinLay_70);
        LinLayerSet[8][0] = findViewById(R.id.LinLay_80);
        LinLayerSet[9][0] = findViewById(R.id.LinLay_90);
        LinLayerSet[0][1] = findViewById(R.id.LinLay_01);
        LinLayerSet[1][1] = findViewById(R.id.LinLay_11);
        LinLayerSet[2][1] = findViewById(R.id.LinLay_21);
        LinLayerSet[3][1] = findViewById(R.id.LinLay_31);
        LinLayerSet[4][1] = findViewById(R.id.LinLay_41);
        LinLayerSet[5][1] = findViewById(R.id.LinLay_51);
        LinLayerSet[6][1] = findViewById(R.id.LinLay_61);
        LinLayerSet[7][1] = findViewById(R.id.LinLay_71);
        LinLayerSet[8][1] = findViewById(R.id.LinLay_81);
        LinLayerSet[9][1] = findViewById(R.id.LinLay_91);
        LinLayerSet[0][2] = findViewById(R.id.LinLay_02);
        LinLayerSet[1][2] = findViewById(R.id.LinLay_12);
        LinLayerSet[2][2] = findViewById(R.id.LinLay_22);
        LinLayerSet[3][2] = findViewById(R.id.LinLay_32);
        LinLayerSet[4][2] = findViewById(R.id.LinLay_42);
        LinLayerSet[5][2] = findViewById(R.id.LinLay_52);
        LinLayerSet[6][2] = findViewById(R.id.LinLay_62);
        LinLayerSet[7][2] = findViewById(R.id.LinLay_72);
        LinLayerSet[8][2] = findViewById(R.id.LinLay_82);
        LinLayerSet[9][2] = findViewById(R.id.LinLay_92);
        LinLayerSet[0][3] = findViewById(R.id.LinLay_03);
        LinLayerSet[1][3] = findViewById(R.id.LinLay_13);
        LinLayerSet[2][3] = findViewById(R.id.LinLay_23);
        LinLayerSet[3][3] = findViewById(R.id.LinLay_33);
        LinLayerSet[4][3] = findViewById(R.id.LinLay_43);
        LinLayerSet[5][3] = findViewById(R.id.LinLay_53);
        LinLayerSet[6][3] = findViewById(R.id.LinLay_63);
        LinLayerSet[7][3] = findViewById(R.id.LinLay_73);
        LinLayerSet[8][3] = findViewById(R.id.LinLay_83);
        LinLayerSet[9][3] = findViewById(R.id.LinLay_93);
        LinLayerSet[0][4] = findViewById(R.id.LinLay_04);
        LinLayerSet[1][4] = findViewById(R.id.LinLay_14);
        LinLayerSet[2][4] = findViewById(R.id.LinLay_24);
        LinLayerSet[3][4] = findViewById(R.id.LinLay_34);
        LinLayerSet[4][4] = findViewById(R.id.LinLay_44);
        LinLayerSet[5][4] = findViewById(R.id.LinLay_54);
        LinLayerSet[6][4] = findViewById(R.id.LinLay_64);
        LinLayerSet[7][4] = findViewById(R.id.LinLay_74);
        LinLayerSet[8][4] = findViewById(R.id.LinLay_84);
        LinLayerSet[9][4] = findViewById(R.id.LinLay_94);
        LinLayerSet[0][5] = findViewById(R.id.LinLay_05);
        LinLayerSet[1][5] = findViewById(R.id.LinLay_15);
        LinLayerSet[2][5] = findViewById(R.id.LinLay_25);
        LinLayerSet[3][5] = findViewById(R.id.LinLay_35);
        LinLayerSet[4][5] = findViewById(R.id.LinLay_45);
        LinLayerSet[5][5] = findViewById(R.id.LinLay_55);
        LinLayerSet[6][5] = findViewById(R.id.LinLay_65);
        LinLayerSet[7][5] = findViewById(R.id.LinLay_75);
        LinLayerSet[8][5] = findViewById(R.id.LinLay_85);
        LinLayerSet[9][5] = findViewById(R.id.LinLay_95);
        LinLayerSet[0][6] = findViewById(R.id.LinLay_06);
        LinLayerSet[1][6] = findViewById(R.id.LinLay_16);
        LinLayerSet[2][6] = findViewById(R.id.LinLay_26);
        LinLayerSet[3][6] = findViewById(R.id.LinLay_36);
        LinLayerSet[4][6] = findViewById(R.id.LinLay_46);
        LinLayerSet[5][6] = findViewById(R.id.LinLay_56);
        LinLayerSet[6][6] = findViewById(R.id.LinLay_66);
        LinLayerSet[7][6] = findViewById(R.id.LinLay_76);
        LinLayerSet[8][6] = findViewById(R.id.LinLay_86);
        LinLayerSet[9][6] = findViewById(R.id.LinLay_96);
        LinLayerSet[0][7] = findViewById(R.id.LinLay_07);
        LinLayerSet[1][7] = findViewById(R.id.LinLay_17);
        LinLayerSet[2][7] = findViewById(R.id.LinLay_27);
        LinLayerSet[3][7] = findViewById(R.id.LinLay_37);
        LinLayerSet[4][7] = findViewById(R.id.LinLay_47);
        LinLayerSet[5][7] = findViewById(R.id.LinLay_57);
        LinLayerSet[6][7] = findViewById(R.id.LinLay_67);
        LinLayerSet[7][7] = findViewById(R.id.LinLay_77);
        LinLayerSet[8][7] = findViewById(R.id.LinLay_87);
        LinLayerSet[9][7] = findViewById(R.id.LinLay_97);
        LinLayerSet[0][8] = findViewById(R.id.LinLay_08);
        LinLayerSet[1][8] = findViewById(R.id.LinLay_18);
        LinLayerSet[2][8] = findViewById(R.id.LinLay_28);
        LinLayerSet[3][8] = findViewById(R.id.LinLay_38);
        LinLayerSet[4][8] = findViewById(R.id.LinLay_48);
        LinLayerSet[5][8] = findViewById(R.id.LinLay_58);
        LinLayerSet[6][8] = findViewById(R.id.LinLay_68);
        LinLayerSet[7][8] = findViewById(R.id.LinLay_78);
        LinLayerSet[8][8] = findViewById(R.id.LinLay_88);
        LinLayerSet[9][8] = findViewById(R.id.LinLay_98);
        LinLayerSet[0][9] = findViewById(R.id.LinLay_09);
        LinLayerSet[1][9] = findViewById(R.id.LinLay_19);
        LinLayerSet[2][9] = findViewById(R.id.LinLay_29);
        LinLayerSet[3][9] = findViewById(R.id.LinLay_39);
        LinLayerSet[4][9] = findViewById(R.id.LinLay_49);
        LinLayerSet[5][9] = findViewById(R.id.LinLay_59);
        LinLayerSet[6][9] = findViewById(R.id.LinLay_69);
        LinLayerSet[7][9] = findViewById(R.id.LinLay_79);
        LinLayerSet[8][9] = findViewById(R.id.LinLay_89);
        LinLayerSet[9][9] = findViewById(R.id.LinLay_99);
    }
}
