import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;

public class Quantizer {
    static Matrix getAverageMatrix(ArrayList<Matrix> matrices) {
        int rows = matrices.get(0).rows;
        int cols = matrices.get(0).cols;
        Matrix averageMatrix = new Matrix(rows, cols);
        for (int i=0; i<rows; i++) {
            for (int j=0; j<cols; j++) {
                int average = 0;
                for (Matrix matrix : matrices) {
                    average += matrix.get(i, j);
                }
                average /= matrices.size();
                averageMatrix.insert(i, j, average);
            }
        }
        return averageMatrix;
    }

    static ArrayList<Matrix> generateBlocks(ArrayList<Matrix> sourceMatrices, int numberOfBlocks) {
        ArrayList<Matrix> breakPoints = null;
        ArrayList<ArrayList<Matrix>> ranges = new ArrayList<>();
        ranges.add((ArrayList<Matrix>) sourceMatrices.clone());
        for (int i=numberOfBlocks; i>1; i /= 2) {
            breakPoints = new ArrayList<>();
            for (ArrayList<Matrix> range : ranges) {
                Matrix[] split = getAverageMatrix(range).splitMatrix();
                breakPoints.add(split[0]);
                breakPoints.add(split[1]);
            }
            ranges = new ArrayList<>();
            //Initialize dummy data into array of size
            for (int j=0; j<breakPoints.size(); j++) {
                ranges.add(new ArrayList<>());
            }
            for (Matrix matrix : sourceMatrices) {
                int breakPointIndex = matrix.getNearestMatrix(breakPoints);
                ranges.get(breakPointIndex).add(matrix);
            }
        }

        //Max 10 iterations to get least error blocks
        for (int i=0; i<10; i++) {
//            ranges = new ArrayList<>();
//            for (int j=0; j<breakPoints.size(); j++) {
//                ranges.add(new ArrayList<>());
//            }
//            for (Matrix matrix : sourceMatrices) {
//                int breakPointIndex = matrix.getNearestMatrix(breakPoints);
//                ranges.get(breakPointIndex).add(matrix);
//            }
            ArrayList<Matrix> newAverages = new ArrayList<>();
            for (ArrayList<Matrix> range : ranges) {
                if (range.size() > 0) {
                    newAverages.add(getAverageMatrix(range));
                }
            }
            if (newAverages.equals(breakPoints)) {
                break;
            }
            breakPoints = newAverages;
            ranges = new ArrayList<>();
            for (int j=0; j<breakPoints.size(); j++) {
                ranges.add(new ArrayList<>());
            }
            for (Matrix matrix : sourceMatrices) {
                int breakPointIndex = matrix.getNearestMatrix(breakPoints);
                ranges.get(breakPointIndex).add(matrix);
            }
        }
        return breakPoints;
    }

    public static void compress(String imagePath, int vectorWidth, int vectorHeight, int codeBookSize) throws IOException {
        int[][] imageArray = ImageFile.getImageMatrix(imagePath, vectorWidth, vectorHeight);
        // Turn into array of matrices
        ArrayList<Matrix> image = new ArrayList<>();
        for (int i=0; i< imageArray.length; i += vectorHeight) {
            for (int j=0; j<imageArray[0].length; j+= vectorWidth) {
                Matrix matrix = new Matrix(vectorHeight, vectorHeight);
                for (int ii=0; ii<vectorWidth; ii++) {
                    for (int jj=0; jj<vectorHeight; jj++) {
                        matrix.insert(ii, jj, imageArray[i+ii][j+jj]);
                    }
                }
                image.add(matrix);
            }
        }

        ArrayList<Matrix> codeBook = generateBlocks(image, codeBookSize);
        ArrayList<Integer> labeledBlocks = new ArrayList<>();
        for (Matrix matrix : image) {
            int label = matrix.getNearestMatrix(codeBook);
            labeledBlocks.add(label);
        }
        ArrayList<ArrayList<ArrayList<Integer>>> book = new ArrayList<>();
        for (Matrix matrix : codeBook) {
            book.add(matrix.getMatrix());
        }

        //Object stream  for serialization
        String compressedPath = imagePath.substring(0, imagePath.lastIndexOf('.')) + ".quantized";
        ObjectOutputStream outObjectStreamer = new ObjectOutputStream(new FileOutputStream(compressedPath));
        System.out.println(imageArray[0].length);
        outObjectStreamer.writeInt(imageArray[0].length); //Image length
        outObjectStreamer.writeInt(imageArray.length); //Image Height
        outObjectStreamer.writeInt(vectorWidth);
        outObjectStreamer.writeInt(vectorHeight);
        outObjectStreamer.writeObject(book);
        outObjectStreamer.writeObject(labeledBlocks);
        FileWriter fw = new FileWriter(new File("trace-log.txt"));
        fw.write("image width: " + imageArray[0].length + "\n");
        fw.write("image height: " + imageArray.length + "\n");
        fw.write("vector width: " + vectorWidth + "\n");
        fw.write("vector height: " + vectorHeight + "\n");
        fw.write("Code book: \n" + book + "\n");
        fw.close();

//        for (int i=0, j=1; i<image.size(); i++, j++) {
//            System.out.print(image.get(i) + ", ");
//            if (j == imageArray[0].length/vectorWidth) {
//                System.out.println();
//                j = 1;
//            }
//        }
//
//        for (int i=0; i< imageArray.length; i++) {
//            for (int j=0; j< imageArray[0].length; j++) {
//                System.out.print(imageArray[i][j] + ", ");
//            }
//            System.out.println();
//        }
    }

    public static void decompress(String path) throws IOException, ClassNotFoundException {
        ObjectInputStream inObjectStreamer = new ObjectInputStream(new FileInputStream(path));

        int imageWidth = inObjectStreamer.readInt();
        int imageHeight = inObjectStreamer.readInt();
        int vectorWidth = inObjectStreamer.readInt();
        int vectorHeight = inObjectStreamer.readInt();
        ArrayList<ArrayList<ArrayList<Integer>>> codeBook = (ArrayList<ArrayList<ArrayList<Integer>>>) inObjectStreamer.readObject();
        ArrayList<Integer> labeledMatrices = (ArrayList<Integer>)  inObjectStreamer.readObject();

        int[][] decompressedImage = new int[imageHeight][imageWidth];
        int x = 0;
        for (int i=0; i<labeledMatrices.size(); i++) {
            int label = labeledMatrices.get(i);
            ArrayList<ArrayList<Integer>> book = codeBook.get(label);
//            System.out.println(book.size());
//            System.out.println(book.get(0).size());
            for (int ii=0; ii<vectorWidth; ii++) {
                for (int jj=0; jj<vectorHeight; jj++) {
                    int col = (i*vectorWidth)%imageWidth + ii;
                    int row = x+jj;
//                    System.out.print("row: " + row);
//                    System.out.print(", col: " + col + ", ");
                    decompressedImage[row][col] = book.get(ii).get(jj);
                }
            }
//            System.out.println();
            if (((i+1)*vectorWidth)%(imageWidth) == 0 && i>0) {
                x++;
            }
        }

//        for (int i=0; i< decompressedImage.length; i++) {
//            for (int j=0; j< decompressedImage[0].length; j++) {
//                System.out.print(decompressedImage[i][j] + ", ");
//            }
//            System.out.println();
//        }

        BufferedImage image = new BufferedImage(decompressedImage[0].length, decompressedImage.length, BufferedImage.TYPE_INT_RGB);
        for (int i= 0; i < decompressedImage.length; i++) {
            for (int j = 0; j < decompressedImage[0].length; j++) {
                int value= 0xff000000 | (decompressedImage[i][j]<<16) | (decompressedImage[i][j]<<8) | (decompressedImage[i][j]);
                image.setRGB(j, i, value);
            }
        }
        String outPath = path.substring(0, path.lastIndexOf('.')) + "-Decompressed.jpg";
        ImageIO.write(image, "jpg", new File(outPath));
//        for (int i=0; i< decompressedImage.length; i++) {
//            for (int j=0; j< decompressedImage[0].length; j++) {
//                System.out.print(decompressedImage[i][j] + ", ");
//            }
//            System.out.println();
//        }

    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
//        ArrayList<Matrix> image = new ArrayList<>();
//        image.add(new Matrix(new int[][]{new int[]{1, 2}, new int[]{3, 4}}));
//        image.add(new Matrix(new int[][]{new int[]{7, 9}, new int[]{6, 6}}));
//        image.add(new Matrix(new int[][]{new int[]{4, 11}, new int[]{12, 12}}));
//        image.add(new Matrix(new int[][]{new int[]{4, 9}, new int[]{10, 10}}));
//        image.add(new Matrix(new int[][]{new int[]{15, 14}, new int[]{20, 18}}));
//        image.add(new Matrix(new int[][]{new int[]{9, 9}, new int[]{8, 8}}));
//        image.add(new Matrix(new int[][]{new int[]{4, 3}, new int[]{4, 5}}));
//        image.add(new Matrix(new int[][]{new int[]{17, 16}, new int[]{18, 18}}));
//        image.add(new Matrix(new int[][]{new int[]{1, 4}, new int[]{5, 6}}));
//
////        System.out.println(image);
//        ArrayList<Matrix> blocks = generateBlocks(image, 4);
//        System.out.println(blocks);
        compress("test2.png", 2, 2, 20);
        decompress("test2.quantized");
    }
}
