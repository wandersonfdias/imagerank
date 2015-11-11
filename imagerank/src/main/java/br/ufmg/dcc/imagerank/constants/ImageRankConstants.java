package br.ufmg.dcc.imagerank.constants;

/**
 * @author Wanderson Ferreira Dias - <code>wandersonf.dias@gmail.com</code>
 */
public interface ImageRankConstants
{
	String PAIR_OUTPUT_FILENAME = "pares.txt";
	String WEKA_PAIR_OUTPUT_FILENAME = "pares.arff";
	String DISCRETIZED_WEKA_PAIR_OUTPUT_FILENAME = "pares_discretizados.arff";
	String LAC_OUTPUT_FILENAME = "lac_result.txt";
	String LAC_TRAINING_FILENAME = "treino.txt";
	String LAC_TEST_FILENAME = "teste.txt";
	String SCORE_OUTPUT_FILENAME = "score.txt";

	String FIELD_SEPARATOR = ",";
	String LINE_SEPARATOR = "\n";
	String FILENAME_SEPARATOR = ".";

	int DESCRIPTOR_PRECISION_VALUE = 10;
	Character PAIR_ID_SEPARATOR = '|';
	String JPEG_IMAGE_EXTENSION = "jpg";
}
