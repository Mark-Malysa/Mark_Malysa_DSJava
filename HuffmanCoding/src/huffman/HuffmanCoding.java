package huffman;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;

import javax.xml.transform.Source;

/**
 * This class contains methods which, when used together, perform the
 * entire Huffman Coding encoding and decoding process
 * 
 * @author Ishaan Ivaturi
 * @author Prince Rawal
 */
public class HuffmanCoding {
    private String fileName;
    private ArrayList<CharFreq> sortedCharFreqList;
    private TreeNode huffmanRoot;
    private String[] encodings;

    /**
     * Constructor used by the driver, sets filename
     * DO NOT EDIT
     * @param f The file we want to encode
     */
    public HuffmanCoding(String f) { 
        fileName = f; 
    }

    /**
     * Reads from filename character by character, and sets sortedCharFreqList
     * to a new ArrayList of CharFreq objects with frequency > 0, sorted by frequency
     */
    public void makeSortedList() {
        sortedCharFreqList = new ArrayList<CharFreq>();
        Boolean infile = false;
        StdIn.setFile(fileName);
        int count = 1;
        Character current = StdIn.readChar();
        sortedCharFreqList.add(new CharFreq(current, 1));
        while(StdIn.hasNextChar()){
            count++;
            current = StdIn.readChar();
            for(int i = 0; i < sortedCharFreqList.size(); i++){
                if(sortedCharFreqList.get(i).getCharacter() == current){
                    sortedCharFreqList.get(i).setProbOcc(sortedCharFreqList.get(i).getProbOcc() + 1.0);
                    infile = true;
                }
            }
            if(!infile){
                CharFreq newChar = new CharFreq(current, 1.0);
                sortedCharFreqList.add(newChar);
            }
            infile = false;
        }
        if(sortedCharFreqList.size() == 1){
            current = sortedCharFreqList.get(0).getCharacter();
            int ascii = (int) current;
            int nextCharacterVal = ascii++;
            if(ascii == 127){
                nextCharacterVal = 0;
            }
            Character asciiToChar = (char) ascii;
            sortedCharFreqList.add(new CharFreq(asciiToChar, 0.0));
        }
        Collections.sort(sortedCharFreqList);
        
        for(int i = 0; i < sortedCharFreqList.size(); i++){
            sortedCharFreqList.get(i).setProbOcc( (sortedCharFreqList.get(i).getProbOcc()) / count);
        }
    }

    /**
     * Uses sortedCharFreqList to build a huffman coding tree, and stores its root
     * in huffmanRoot
     */
    public void makeTree() {
        Queue<TreeNode> source = new Queue<TreeNode>();
        Queue<TreeNode> target = new Queue<TreeNode>();

        for(int i = 0; i < sortedCharFreqList.size(); i++){
            source.enqueue(new TreeNode(sortedCharFreqList.get(i), null, null));
        }

        TreeNode firstDeq = source.dequeue();
        TreeNode secDeq = source.dequeue();
        CharFreq mergedChar = new CharFreq(null, firstDeq.getData().getProbOcc() + secDeq.getData().getProbOcc());
        TreeNode mergedNode = new TreeNode(mergedChar, firstDeq, secDeq);
        target.enqueue(mergedNode);

        Queue<TreeNode> twoSmallest = new Queue<TreeNode>(); 

        while((source.size() + target.size() > 1)){

            while(twoSmallest.size() < 2){
                if(source.isEmpty()){
                    twoSmallest.enqueue(target.dequeue());
                    continue;
                }
                if(target.isEmpty()){
                    twoSmallest.enqueue(source.dequeue());
                    continue;
                }
                if(source.peek().getData().getProbOcc() <= target.peek().getData().getProbOcc()){
                    twoSmallest.enqueue(source.dequeue());
                }
                else{
                    twoSmallest.enqueue(target.dequeue());
                }
            }

            firstDeq = twoSmallest.dequeue();
            secDeq = twoSmallest.dequeue();
            mergedChar = new CharFreq(null, firstDeq.getData().getProbOcc() + secDeq.getData().getProbOcc());
            mergedNode = new TreeNode(mergedChar, firstDeq, secDeq);
            target.enqueue(mergedNode);

            twoSmallest = new Queue<TreeNode>();  
        }

        if(target.size() > 1){
            firstDeq = target.dequeue();
            secDeq = target.dequeue();
            mergedChar = new CharFreq(null, firstDeq.getData().getProbOcc() + secDeq.getData().getProbOcc());
            mergedNode = new TreeNode(mergedChar, firstDeq, secDeq);
            target.enqueue(mergedNode);
        }

        huffmanRoot = target.peek();
    }

    /**
     * Uses huffmanRoot to create a string array of size 128, where each
     * index in the array contains that ASCII character's bitstring encoding. Characters not
     * present in the huffman coding tree should have their spots in the array left null.
     * Set encodings to this array.
     */
    public void makeEncodings() {
        encodings = new String[128];
        encodingHelper(huffmanRoot.getLeft(), null, "0");
        encodingHelper(huffmanRoot.getRight(), null, "1");

    
    }

    public void encodingHelper(TreeNode parent, Character c, String s){
        if(c != null){
            int ascii = (int) c;
            encodings[ascii] = s;
            return;
        }
        else if(parent.getLeft() == null){
            encodingHelper(parent, parent.getData().getCharacter(), s);
        }
        else{
            encodingHelper(parent.getLeft(), c, s + "0");
            encodingHelper(parent.getRight(), c, s + "1");
        }
    }



    /**
     * Using encodings and filename, this method makes use of the writeBitString method
     * to write the final encoding of 1's and 0's to the encoded file.
     * 
     * @param encodedFile The file name into which the text file is to be encoded
     */
    public void encode(String encodedFile) {
        StdIn.setFile(fileName);
        String onesAndZeros = "";
        while(StdIn.hasNextChar()){
            char nextChar = StdIn.readChar();
            int ascii = (int) nextChar;
            onesAndZeros += encodings[ascii];
        }

        writeBitString(encodedFile, onesAndZeros);
    }
    
    /**
     * Writes a given string of 1's and 0's to the given file byte by byte
     * and NOT as characters of 1 and 0 which take up 8 bits each
     * DO NOT EDIT
     * 
     * @param filename The file to write to (doesn't need to exist yet)
     * @param bitString The string of 1's and 0's to write to the file in bits
     */
    public static void writeBitString(String filename, String bitString) {
        byte[] bytes = new byte[bitString.length() / 8 + 1];
        int bytesIndex = 0, byteIndex = 0, currentByte = 0;

        // Pad the string with initial zeroes and then a one in order to bring
        // its length to a multiple of 8. When reading, the 1 signifies the
        // end of padding.
        int padding = 8 - (bitString.length() % 8);
        String pad = "";
        for (int i = 0; i < padding-1; i++) pad = pad + "0";
        pad = pad + "1";
        bitString = pad + bitString;

        // For every bit, add it to the right spot in the corresponding byte,
        // and store bytes in the array when finished
        for (char c : bitString.toCharArray()) {
            if (c != '1' && c != '0') {
                System.out.println("Invalid characters in bitstring");
                return;
            }

            if (c == '1') currentByte += 1 << (7-byteIndex);
            byteIndex++;
            
            if (byteIndex == 8) {
                bytes[bytesIndex] = (byte) currentByte;
                bytesIndex++;
                currentByte = 0;
                byteIndex = 0;
            }
        }
        
        // Write the array of bytes to the provided file
        try {
            FileOutputStream out = new FileOutputStream(filename);
            out.write(bytes);
            out.close();
        }
        catch(Exception e) {
            System.err.println("Error when writing to file!");
        }
    }

    /**
     * Using a given encoded file name, this method makes use of the readBitString method 
     * to convert the file into a bit string, then decodes the bit string using the 
     * tree, and writes it to a decoded file. 
     * 
     * @param encodedFile The file which has already been encoded by encode()
     * @param decodedFile The name of the new file we want to decode into
     */
    public void decode(String encodedFile, String decodedFile) {
        StdOut.setFile(decodedFile);
        String onesAndZeros = readBitString(encodedFile);
        String decodedString = "";

        TreeNode currentNode = huffmanRoot;
        for(int i = 0; i < onesAndZeros.length(); i++){

            char currentCharacter = onesAndZeros.charAt(i);
            
            if(currentCharacter == '0'){
                    currentNode = currentNode.getLeft();
                }

            else{
                    currentNode = currentNode.getRight();
                }
            
            if(currentNode.getLeft() == null){
                decodedString += currentNode.getData().getCharacter();
                currentNode = huffmanRoot;
            }
        }

        StdOut.print(decodedString.toString());
    }

    /**
     * Reads a given file byte by byte, and returns a string of 1's and 0's
     * representing the bits in the file
     * DO NOT EDIT
     * 
     * @param filename The encoded file to read from
     * @return String of 1's and 0's representing the bits in the file
     */
    public static String readBitString(String filename) {
        String bitString = "";
        
        try {
            FileInputStream in = new FileInputStream(filename);
            File file = new File(filename);

            byte bytes[] = new byte[(int) file.length()];
            in.read(bytes);
            in.close();
            
            // For each byte read, convert it to a binary string of length 8 and add it
            // to the bit string
            for (byte b : bytes) {
                bitString = bitString + 
                String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
            }

            // Detect the first 1 signifying the end of padding, then remove the first few
            // characters, including the 1
            for (int i = 0; i < 8; i++) {
                if (bitString.charAt(i) == '1') return bitString.substring(i+1);
            }
            
            return bitString.substring(8);
        }
        catch(Exception e) {
            System.out.println("Error while reading file!");
            return "";
        }
    }

    /*
     * Getters used by the driver. 
     * DO NOT EDIT or REMOVE
     */

    public String getFileName() { 
        return fileName; 
    }

    public ArrayList<CharFreq> getSortedCharFreqList() { 
        return sortedCharFreqList; 
    }

    public TreeNode getHuffmanRoot() { 
        return huffmanRoot; 
    }

    public String[] getEncodings() { 
        return encodings; 
    }
}
