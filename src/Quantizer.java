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
            for (int ii=0; ii<vectorWidth; ii++) {
                for (int jj=0; jj<vectorHeight; jj++) {
                    int col = (i*vectorWidth)%imageWidth + ii;
                    int row = x+jj;
                    decompressedImage[row][col] = book.get(ii).get(jj);
                }
            }
            if (((i+1)*vectorWidth)%(imageWidth) == 0 && i>0) {
                x+=vectorHeight;
            }
        }

        BufferedImage image = new BufferedImage(decompressedImage[0].length, decompressedImage.length, BufferedImage.TYPE_INT_RGB);
        for (int i= 0; i < decompressedImage.length; i++) {
            for (int j = 0; j < decompressedImage[0].length; j++) {
                int value= 0xff000000 | (decompressedImage[i][j]<<16) | (decompressedImage[i][j]<<8) | (decompressedImage[i][j]);
                image.setRGB(j, i, value);
            }
        }
        String outPath = path.substring(0, path.lastIndexOf('.')) + "-Decompressed.jpg";
        ImageIO.write(image, "jpg", new File(outPath));
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        compress("test.png", 2, 2, 60);
        decompress("test.quantized");
    }
}
