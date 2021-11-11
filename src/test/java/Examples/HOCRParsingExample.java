package Examples;

import io.github.karols.hocr4j.Line;
import io.github.karols.hocr4j.LineThat;
import io.github.karols.hocr4j.Page;
import io.github.karols.hocr4j.dom.HocrParser;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author adminu
 */
public class HOCRParsingExample {

    static final String SAMPLES_PATH = "./samples/";

    static final String HOCR_FILE_NAME = "gnu";

    public static String readFile(String path)
            throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, Charset.defaultCharset());
    }

    public static void main(String[] args) {

        try {
            String hocr = readFile(SAMPLES_PATH + HOCR_FILE_NAME + ".hocr");
            
             List<Page> pages = HocrParser.parse(hocr);
             Page page1=pages.get(0);
             
            System.out.println("===============find the line contains a word from page===================="); 
            List<Line> lines = page1.findAllLines(LineThat.contains("GNU"));
            for (Line line : lines) {
                System.out.println(line.mkRoughString());

            }
           
            System.out.println("===============   Dumping the full page line by line   ===================="); 
            for (Line line : pages.get(0).getAllLines()) {
                System.out.println(line.mkRoughString());

            }
        } catch (IOException ex) {
            Logger.getLogger(HOCRParsingExample.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
