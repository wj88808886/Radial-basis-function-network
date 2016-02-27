import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RBF {

	private Point[] traindata = new Point[101];
	private int k;
	private double eta;
	private Cluster[] kclusters;
	private boolean samestd;
	private ArrayList<Base> bases;
	private Output output;
	private final double EPS = 1e-8;
	private BufferedWriter writer;

	public RBF(int k, double eta, boolean samestd) {
		this.k = k;
		this.eta = eta;
		this.kclusters = new Cluster[k];
		this.samestd = samestd;
		this.bases = new ArrayList<Base>();
	}

	private double h(double x) {
		return 0.5 + 0.4 * Math.sin(2 * Math.PI * x);
	}

	private void generate_train() {
		for (int i = 0; i < traindata.length; i++) {
			double x = Math.random();
			traindata[i] = new Point(x, h(x) + Math.random() * 0.1);
		}
	}

	private double mean(List<Double> a) {
		double mean = 0;
		for (int i = 0; i < a.size(); i++) {
			mean += a.get(i);
		}
		return mean / a.size();
	}

	private double var(List<Double> a) {
		double var = 0;
		double mean = mean(a);
		for (int i = 0; i < a.size(); i++) {
			var += (a.get(i) - mean) * (a.get(i) - mean);
		}
		return var / a.size();
	}

	private double meanvar(Cluster[] kclusters) {
		double meanvar = 0;
		int count = 0;
		for (int i = 0; i < k; i++) {
			if (kclusters[i].points().size() == 0) {
				count++;
				meanvar += kclusters[i].var();
			}
		}
		return meanvar / count;
	}

	private void Kmean() {
		Point[] randomarray = traindata.clone();
		boolean flag = true;

		for (int i = 0; i < k; i++) {
			int index = (int) (Math.random() * (traindata.length - i));
			Point temp = randomarray[index];
			kclusters[i] = new Cluster(temp.x());
			randomarray[index] = randomarray[traindata.length - i - 1];
			randomarray[traindata.length - i - 1] = temp;
		}

		do {
			flag = true;
			for (int i = 0; i < k; i++) {
				kclusters[i].removeAll();
			}
			for (int i = 0; i < traindata.length; i++) {
				Point temp = traindata[i];
				double x = temp.x();
				double min = Double.MAX_VALUE;
				int index = -1;
				for (int j = 0; j < k; j++) {
					double current = (x - kclusters[j].mean())
							* (x - kclusters[j].mean());
					if (min > current) {
						min = current;
						index = j;
					}
				}
				temp.setk(index);
			}

			for (int i = 0; i < traindata.length; i++) {
				kclusters[traindata[i].k()].append(traindata[i].x());
			}

			for (int i = 0; i < k; i++) {
				double mean = mean(kclusters[i].points());
				if (Math.abs(mean - kclusters[i].mean()) > EPS)
					flag = false;
				kclusters[i].setmean(mean);

			}

		} while (!flag);
		for (int i = 0; i < k; i++) {
			kclusters[i].setvar(var(kclusters[i].points()));
		}
		double meanvar = meanvar(kclusters);
		double dvar = 0;

		if (samestd) {
			for (int i = 0; i < k; i++) {
				for (int j = i + 1; j < k; j++) {
					dvar = Math.max(Math.abs(kclusters[i].mean()
									- kclusters[j].mean()), dvar);
				}
			}
		}
		dvar /= Math.sqrt(2 * k);
		dvar *= dvar;
		for (int i = 0; i < k; i++) {
			if (kclusters[i].points().size() == 1)
				kclusters[i].setvar(meanvar);
			if (samestd)
				kclusters[i].setvar(dvar);
		}
		for (int i = 0; i < k; i++) {
			System.out.println("mean = " + kclusters[i].mean() + " variance = "
					+ kclusters[i].var());
		}
	}

	private double phi(double mean, double std, double x) {
		return Math.exp(-1 * (1 / (2 * std * std)) * (mean - x) * (mean - x));
	}

	private double sigmod(double x) {
		return x;
		// return (1 / (1 + Math.exp(-x)));
	}

	private double sigmodp(double x) {
		return 1;
		// return sigmod(x) * (1 - sigmod(x));
	}

	private void initialize() {
		for (int i = 0; i < k; i++) {
			Base base = new Base(Math.random() * 2 - 1);
			bases.add(base);
		}
		output = new Output(Math.random() * 2 - 1);
	}

	private void train() {
		for (int i = 0; i < 100; i++) {
			for (int j = 0; j < traindata.length; j++) {
				for (int l = 0; l < k; l++) {
					Cluster c = kclusters[l];
					bases.get(l).setx(phi(c.mean(), c.std(), traindata[j].x()));
				}
				forward();
				double e = traindata[j].y() - output.y();
				output.setdelta(e * sigmodp(output.v()));
				for (int l = 0; l < k; l++) {
					Base base = bases.get(l);
					base.setweight(base.weight() + eta * output.delta()
							* base.x());
				}
				output.setbias(output.bias() + eta * output.delta());
			}
		}
		System.out.println("w0 = " + bases.get(0).weight());
		System.out.println("w1 = " + bases.get(1).weight());
		System.out.println("bias = " + output.bias());
	}

	private void forward() {
		double temp = 0;
		for (int i = 0; i < k; i++) {
			Base current = bases.get(i);
			temp += current.x() * current.weight();
		}
		temp += output.bias();
		output.setv(temp);
		temp = sigmod(temp);
		output.sety(temp);
	}

	private void getResults() throws IOException {
		File file = new File("results.csv");
		writer = new BufferedWriter(new FileWriter(file));
		double xi = 0;
		for (int j = 0; j < 100; j++) {
			for (int l = 0; l < k; l++) {
				Cluster c = kclusters[l];
				bases.get(l).setx(phi(c.mean(), c.std(), xi));
			}
			forward();
			String text = xi + " , " + h(xi) + " , " + output.y();
			System.out.println(text);
			writer.write(text + "\n");
			xi += 0.01;
		}
		writer.close();
		File file2 = new File("origin.csv");
		writer = new BufferedWriter(new FileWriter(file2));
		for (int j = 0; j < traindata.length; j++) {
			writer.write(traindata[j].x() + " , " + traindata[j].y() + "\n");
		}
		writer.close();
	}

	public static void run(int k, double eta, boolean samestd)
			throws IOException {
		RBF rbf = new RBF(k, eta, samestd);
		rbf.generate_train();
		rbf.Kmean();
		rbf.initialize();
		rbf.train();
		rbf.getResults();

	}

	public static void main(String[] args) throws IOException {
		run(2, 0.02, true);
	}

}
