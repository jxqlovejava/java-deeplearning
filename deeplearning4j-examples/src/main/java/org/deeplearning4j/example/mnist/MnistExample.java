package org.deeplearning4j.example.mnist;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.deeplearning4j.datasets.DataSet;
import org.deeplearning4j.datasets.iterator.DataSetIterator;
import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;
import org.deeplearning4j.dbn.DBN;
import org.deeplearning4j.eval.Evaluation;
import org.jblas.DoubleMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MnistExample {

	private static Logger log = LoggerFactory.getLogger(MnistExample.class);
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		//batches of 10, 60000 examples total
		DataSetIterator iter = new MnistDataSetIterator(10,500);
		
		//784 input (number of columns in mnist, 10 labels (0-9), no regularization
		DBN dbn = new DBN.Builder().useAdaGrad(true).useRegularization(false)
		.hiddenLayerSizes(new int[]{500,400,250}).normalizeByInputRows(true)
		.numberOfInputs(784).numberOfOutPuts(10)
		.build();
		
		while(iter.hasNext()) {
			DataSet next = iter.next();
			dbn.pretrain(next.getFirst(), 1, 0.01, 10000);
		}
		
		iter.reset();
		while(iter.hasNext()) {
			DataSet next = iter.next();
			dbn.setInput(next.getFirst());
			dbn.finetune(next.getSecond(), 0.01, 10000);
		}
		
		
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream("mnist-dbn.bin"));
		dbn.write(bos);
		bos.flush();
		bos.close();
		log.info("Saved dbn");
		
		
		iter.reset();
		
		Evaluation eval = new Evaluation();
		
		while(iter.hasNext()) {
			DataSet next = iter.next();
			DoubleMatrix predict = dbn.predict(next.getFirst());
			DoubleMatrix labels = next.getSecond();
			eval.eval(labels, predict);
		}
		
		log.info("Prediciton f scores and accuracy");
		log.info(eval.stats());
		
		
	}

}
