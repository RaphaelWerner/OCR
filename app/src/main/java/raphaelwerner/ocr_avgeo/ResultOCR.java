package raphaelwerner.ocr_avgeo;


import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.SparseArray;
import android.widget.Spinner;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ResultOCR {

    public String[] inspectFromBitmap(Bitmap bitmap, Context context, int TipoDocumento) {
        String[] OCRResult = new String[4];
        TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();
        try {
            if (!textRecognizer.isOperational()) {

            }

            Frame frame = new Frame.Builder().setBitmap(bitmap).build();
            SparseArray<TextBlock> origTextBlocks = textRecognizer.detect(frame);
            List<TextBlock> textBlocks = new ArrayList<>();
            for (int i = 0; i < origTextBlocks.size(); i++) {
                String line;
                TextBlock textBlock = origTextBlocks.valueAt(i);
                textBlocks.add(textBlock );
            }
            Collections.sort(textBlocks, new Comparator<TextBlock>() {
                @Override
                public int compare(TextBlock o1, TextBlock o2) {
                    int diffOfTops = o1.getBoundingBox().top - o2.getBoundingBox().top;
                    int diffOfLefts = o1.getBoundingBox().left - o2.getBoundingBox().left;
                    if (diffOfTops != 0) {
                        return diffOfTops;
                    }
                    return diffOfLefts;
                }
            });

            boolean aux = false;
            int qtdInteiros = 0;
            int qtdChar = 0;
            int qtdBarra = 0;
            int count = 0;
            boolean auxNomeCNH = true;
            for (TextBlock textBlock : textBlocks) {
                if (textBlock != null && textBlock.getValue() != null) {
                    count++;

                    //verificar o numero de Inteiros da Linha
                    for (int i = 0; i < textBlock.getValue().length(); i++) {
                        if (Character.isDigit(textBlock.getValue().charAt(i))) {
                            qtdInteiros++;
                        }
                        if (Character.isLetter(textBlock.getValue().charAt(i))) {
                            qtdChar++;
                        }
                        if (textBlock.getValue().charAt(i) == '/') {
                            qtdBarra++;
                        }
                    }
                    try {
                        if (TipoDocumento == 0) {
                            // verificar se é um RG
                            if (qtdInteiros == 8 && qtdBarra <= 1) {
                                OCRResult[1] = textBlock.getValue().replace(" ", "");
                                aux = true;
                            }
                            // verificar se é um NOME
                            if (aux && qtdInteiros == 0 && qtdChar > 5) {
                                 OCRResult[0] = textBlock.getValue().replace("NOME", "");
                                aux = false;
                            }
                            // verificar se é um CPF
                            if (qtdInteiros == 11 || qtdInteiros == 10 && qtdBarra == 0) {
                                OCRResult[2] = textBlock.getValue().replace(" ", "");
                            }
                            //verificar DATA de Nascimento
                            if (qtdBarra == 2 && qtdInteiros > 5 && count > 4) {
                               OCRResult[3] = textBlock.getValue().replace(" ", "");
                            }
                        }






                        boolean cpfdatanasci = true;
                        if (TipoDocumento == 1) {

                            // verificar se é um nome
                            if (auxNomeCNH) {
                                OCRResult[0] = textBlock.getValue();
                                auxNomeCNH = false;
                            }
                            if (textBlock.getValue().contains("NOME") && textBlock.getValue().length() < 6) {
                                auxNomeCNH = true;
                            }
                            if (textBlock.getValue().contains("NOME") && textBlock.getValue().length() > 6) {
                                OCRResult[0] = textBlock.getValue().replace("NOME", "").trim();
                            }

                            // verificar se é um RG
                            if (qtdInteiros == 8 & qtdBarra <= 1) {
                                if (qtdInteiros == 8 & qtdChar > 5) {
                                    int auxFinalRG = 0;
                                    for (int i = 0; i < textBlock.getValue().length(); i++) {
                                        if (Character.isDigit(textBlock.getValue().charAt(i))) {
                                            auxFinalRG = i;
                                        }
                                    }
                                    int auxInicioRG = auxFinalRG;
                                    while (Character.isDigit(textBlock.getValue().charAt(auxInicioRG))) {
                                        auxInicioRG--;
                                    }
                                    String RG = "";
                                    for (int i = auxInicioRG - 1; i <= auxFinalRG; i++) {
                                        RG += textBlock.getValue().charAt(i);
                                    }
                                    OCRResult[1] = RG.replace(" ", "");
                                } else {
                                    OCRResult[1] = ";";
                                    OCRResult[1] = textBlock.getValue().replace(" ", "");
                                }

                            }
                            // verificar data de nascimento e CPF
                            if (qtdBarra == 2 && qtdInteiros > 5 && count > 4 && cpfdatanasci) {
                                String auxDataNascimento = "";
                                for (int i = 0; i < textBlock.getValue().length(); i++) {
                                    if (textBlock.getValue().charAt(i) == ' ' ||
                                            textBlock.getValue().charAt(i) == '.' ||
                                            textBlock.getValue().charAt(i) == '-' ||
                                            textBlock.getValue().charAt(i) == '/' ||
                                            Character.isDigit(textBlock.getValue().charAt(i))) {

                                        auxDataNascimento += textBlock.getValue().charAt(i);
                                    }
                                }
                                int auxCPF = 0;
                                String CPF = "";
                                while (auxDataNascimento.charAt(auxCPF) != '/') {
                                    auxCPF++;
                                }
                                for (int i = 0; i < auxCPF - 2; i++) {
                                    CPF += auxDataNascimento.charAt(i);
                                }
                                int auxDN = auxCPF - 2;
                                String DN = "";
                                for (int i = auxDN; i < auxDataNascimento.length(); i++) {
                                    DN += auxDataNascimento.charAt(i);
                                }

                                OCRResult[2] = CPF.replace(" ", "");
                                String auxDate = DN.replace(" ", "")
                                        .replace("-", "");
                                String year = "";

                                for(int i = 4; i>0; i++){
                                    year += auxDate.charAt(auxDate.length()-i);
                                }

                                if(Calendar.getInstance().get(Calendar.YEAR) - Integer.parseInt(year) >= 18 ){
                                    OCRResult[3] = DN.replace(" ", "")
                                            .replace("-", "");
                                }
                                cpfdatanasci = false;

                            }
                        }
                    }catch(Exception e){ }
                }

                qtdInteiros = 0;
                qtdChar = 0;
                qtdBarra = 0;
            }
           return OCRResult;
        }
        finally {
            textRecognizer.release();
        }
    }


}
