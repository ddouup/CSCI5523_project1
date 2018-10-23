import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Set;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

class TreeNode {
	private String id;
	private int count;
	private TreeNode parentNode;
	private List<TreeNode> childNodes = new ArrayList<TreeNode>();
	private TreeNode nextNode;

	TreeNode(){}
	TreeNode(String id) {
        this.id = id;
    }
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public TreeNode getParentNode() {
		return parentNode;
	}
	public void setParentNode(TreeNode parentNode) {
		this.parentNode = parentNode;
	}
	public List<TreeNode> getChildNodes() {
		return childNodes;
	}
	public void setChildNodes(List<TreeNode> childNodes) {
		this.childNodes = childNodes;
	}
	public void addChildNode(TreeNode node) {
		if (getChildNodes() == null) {
			List<TreeNode> list = new ArrayList<TreeNode>();
			list.add(node);
			setChildNodes(list);
		}
		else {
			getChildNodes().add(node);
		}
	}
	public TreeNode findChildNode(String id) {
		List<TreeNode> childNodes = this.getChildNodes();
		if(childNodes != null) {
			for(TreeNode temp : childNodes) {
				if(temp.getId().equals(id)) {
					return temp;
				}
			}
		}
		return null;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public void increaseCount() {
		this.count = count + 1;
	}
	public TreeNode getNextNode() {
		return nextNode;
	}
	public void setNextNode(TreeNode nextNode) {
		this.nextNode = nextNode;
	}
}

class FPTree {
	private int _minsup;
	private float _minconf;
	private int _option;
	private LinkedHashMap<List<String>, Integer> _frequentItemsets = new LinkedHashMap<List<String>, Integer>();

	FPTree(){}

	FPTree(int minsup, float minconf, int option){
		_minsup = minsup;
		_minconf = minconf;
		_option = option;
	}

	public LinkedHashMap<List<String>, Integer> getFrequentItemsets() {
		return _frequentItemsets;
	}

	public void printTree(TreeNode root) {

		System.out.println(root.getId()+": "+root.getCount());
		if (root.getChildNodes() != null) {
			for (TreeNode node: root.getChildNodes()) {
				printTree(node);
			}
		}
		System.out.println("");
	}

	public void build(LinkedHashMap<Integer, LinkedList<String>> transactions, LinkedList<String> postPattern) {
		ArrayList<TreeNode> headerTable = buildHeaderTable(transactions, _option);

		TreeNode root = buildFPTree(transactions, headerTable);

		if (root.getChildNodes()==null || root.getChildNodes().size() == 0)
            return;

		for (TreeNode pattern : headerTable) {
			LinkedList<String> itemset = new LinkedList<String>();
			itemset.add(pattern.getId());
			if (postPattern != null)
				itemset.addAll(postPattern);

			_frequentItemsets.put(itemset, pattern.getCount());

			LinkedList<String> new_postPattern = new LinkedList<String>();
			new_postPattern.add(pattern.getId());
			if (postPattern != null)
				new_postPattern.addAll(postPattern);

			LinkedHashMap<Integer, LinkedList<String>> new_trans = new LinkedHashMap<Integer, LinkedList<String>>();
			TreeNode nextNode = pattern.getNextNode();
			int index = 1;
			while (nextNode != null) {
				int count = nextNode.getCount();
				LinkedList<String> prenodes = new LinkedList<String>();
				TreeNode parent = nextNode;
				while ((parent = parent.getParentNode()).getId() != null) {
					prenodes.push(parent.getId());
				}

				while (count-- > 0) {
					new_trans.put(index, prenodes);
					index += 1;
				}
				nextNode = nextNode.getNextNode();
			}
			//System.out.println("No. of frequent itemsets:"+_frequentItemsets.size());
			//System.out.println(new_trans);
			//System.out.println(new_postPattern);
			build(new_trans, new_postPattern);
		}
	}

	public ArrayList<TreeNode> buildHeaderTable(LinkedHashMap<Integer, LinkedList<String>> transactions, int option) {
		LinkedHashMap<Integer, Integer> unsorted_items = new LinkedHashMap<Integer, Integer>();
        for (Map.Entry<Integer, LinkedList<String>> transaction : transactions.entrySet()) {
            for (String item : transaction.getValue()) {
                Integer count = unsorted_items.get(Integer.parseInt(item));
                if (count == null) {
                    count = new Integer(0);
                }
                unsorted_items.put(Integer.parseInt(item), ++count);
            }
        }
        /*	Sort the items based on the option
			options = 1
			The numbering of the items coming from the input file is used as the lexicographical ordering of the items.
			options = 2
			The lexicographical ordering of the items is determined by sorting the items in increasing frequency order in each projected database.
			options = 3
			The lexicographical ordering of the items is determined by sorting the items in decreasing frequency order in each projected database.
		*/
		LinkedHashMap<Integer, Integer> itemsets = new LinkedHashMap<Integer, Integer>();
		if (option == 1) {
			LinkedHashMap<Integer, Integer> temp = new LinkedHashMap<Integer, Integer>();
			unsorted_items.entrySet().stream()
				.sorted(Map.Entry.comparingByKey())
				.forEachOrdered(x -> temp.put(x.getKey(), x.getValue()));
			itemsets = temp;
		}
		if (option == 2) {
			LinkedHashMap<Integer, Integer> temp = new LinkedHashMap<Integer, Integer>();
			unsorted_items.entrySet().stream()
				.sorted(Map.Entry.comparingByValue())
				.forEachOrdered(x -> temp.put(x.getKey(), x.getValue()));
			itemsets = temp;
		}
		if (option == 3) {
			LinkedHashMap<Integer, Integer> temp = new LinkedHashMap<Integer, Integer>();
			unsorted_items.entrySet().stream()
				.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
				.forEachOrdered(x -> temp.put(x.getKey(), x.getValue()));
			itemsets = temp;
		}

		ArrayList<TreeNode> headerTable = new ArrayList<TreeNode>();
		//System.out.println("HeaderTable:");
		for (Map.Entry<Integer, Integer>item : itemsets.entrySet()) {
			//Only store items support count >= minsup
			if (item.getValue() >= _minsup) {
				//System.out.println(item);
				TreeNode node = new TreeNode(Integer.toString(item.getKey()));
				node.setCount(item.getValue());
				headerTable.add(node);
			}
		}
		return headerTable;
	}

	public TreeNode buildFPTree(LinkedHashMap<Integer, LinkedList<String>> transactions, ArrayList<TreeNode> headerTable) {
		TreeNode root = new TreeNode();
		TreeNode node_temp = root;

        for (Map.Entry<Integer, LinkedList<String>> unsorted_trans : transactions.entrySet()) {
            LinkedList<String> transaction = transactionSort(unsorted_trans, headerTable);

            for(String id : transaction) {
				TreeNode temp = node_temp.findChildNode(id);
				if(temp == null) {
					temp = new TreeNode();
					temp.setId(id);
					temp.setCount(0);
					temp.setParentNode(node_temp);
					node_temp.addChildNode(temp);
					addNextNode(temp, headerTable);
				}
				temp.increaseCount();
				node_temp = temp;
			}
			node_temp = root;
        }
        return root;
    }

	public  LinkedList<String> transactionSort(Map.Entry<Integer, LinkedList<String>> unsorted_trans, ArrayList<TreeNode> headerTable) {
		LinkedList<String> items = unsorted_trans.getValue();
		LinkedList<String> transaction = new LinkedList<String>(); 

		HashMap<String, Integer> orderMap = new HashMap<String, Integer>();
		for (String item : items) {
			for (int i = 0; i < headerTable.size(); i++) {
				if (headerTable.get(i).getId().equals(item)) {
					orderMap.put(item, i);
					transaction.add(item);
				}
			}
		}

		Collections.sort(transaction, new Comparator<String>(){
			@Override
			public int compare(String key1, String key2) {
				return orderMap.get(key1) - orderMap.get(key2);
			}
		});

		return transaction;
	}

	public void addNextNode(TreeNode temp, ArrayList<TreeNode> headerTable) {
		for(TreeNode node : headerTable) {
			if(node.getId().equals(temp.getId())) {
				while(node.getNextNode() != null) {
					node = node.getNextNode();
				}
				node.setNextNode(temp);
			}
		}
	}
}

class Rule {
	public List<String> lhs;
	public List<String> rhs;
	public int support;
	public float confidence;

	Rule(){}

	Rule(List<String> lhs, List<String> rhs, int support, float confidence){
		this.lhs = lhs;
		this.rhs = rhs;
		this.support = support;
		this.confidence = confidence;
	}
}

public class hcrminer {
	public static int _minsup;
	public static float _minconf;
	public static String _inputfile;
	public static String _outputfile;
	public static int _option;

	//_transactions <id, item item item...>
	public static LinkedHashMap<Integer, LinkedList<String>> _transactions;
	public static LinkedHashMap<String, Integer> _itemsets;

	public static FPTree _FPTree;

	public static LinkedHashMap<List<String>, Integer>  _frequentItemsets;
	public static LinkedList<Rule> _rules;

	hcrminer(int minsup, float minconf, String inputfile, String outputfile, int option) throws IOException {
		_minsup = minsup;
		_minconf = minconf;
		_inputfile = inputfile;
		_outputfile = outputfile;
		_option = option;

		_transactions = new LinkedHashMap<Integer, LinkedList<String>>();
		_itemsets = new LinkedHashMap<String, Integer>();

		_frequentItemsets = new LinkedHashMap<List<String>, Integer>();
		_rules = new LinkedList<Rule>();

		System.out.println(_minsup);
		System.out.println(_minconf);
		System.out.println(_inputfile);
		System.out.println(_outputfile);
		System.out.println(_option);
		System.out.println("");
	}

	public static void readfile(String inputfile, int option) throws IOException {	
		BufferedReader bf = new BufferedReader(new FileReader(inputfile));
		String str;
		int trans_id = 0;
		LinkedList<String> items_id = new LinkedList<String>();
		LinkedHashMap<String, Integer> unsorted_items = new LinkedHashMap<String, Integer>();

		while ((str = bf.readLine()) != null) {
			int trans_id_temp = Integer.parseInt(str.split(" ")[0]);
			String items_id_temp = str.split(" ")[1];

			int count = unsorted_items.containsKey(items_id_temp) ? unsorted_items.get(items_id_temp) : 0;
			unsorted_items.put(items_id_temp, count + 1);

			if (trans_id_temp == trans_id) {
				items_id.add(items_id_temp);
			}
			else {
				_transactions.put(trans_id, items_id);
				//System.out.println(trans_id+": "+items_id);
				trans_id = trans_id_temp;
				items_id = new LinkedList<String>();
				items_id.add(items_id_temp);
			}
		}
		_transactions.put(trans_id, items_id);
		//System.out.println(trans_id+": "+items_id);

		/*	Sort the items based on the option
			options = 1
			The numbering of the items coming from the input file is used as the lexicographical ordering of the items.
			options = 2
			The lexicographical ordering of the items is determined by sorting the items in increasing frequency order in each projected database.
			options = 3
			The lexicographical ordering of the items is determined by sorting the items in decreasing frequency order in each projected database.
		*/
		if (_option == 1) {		
			//The LinkedHashMap already sorted based on input order
			_itemsets = unsorted_items;
		}
		if (_option == 2) {
			unsorted_items.entrySet().stream()
				.sorted(Map.Entry.comparingByValue())
				.forEachOrdered(x -> _itemsets.put(x.getKey(), x.getValue()));
		}
		if (_option == 3) {
			unsorted_items.entrySet().stream()
				.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
				.forEachOrdered(x -> _itemsets.put(x.getKey(), x.getValue()));
		}
	}
	
	public static LinkedList<Rule> generateRules() {
		for (Map.Entry<List<String>, Integer> itemset : _frequentItemsets.entrySet()) {

			List<List<String>> consequente_1 = new ArrayList<List<String>>();
			for (String item : itemset.getKey()) {
				List<String> temp = new ArrayList<String>();
				temp.add(item);
				consequente_1.add(temp);
			}

			genrules(itemset.getKey(), consequente_1, 1, itemset.getValue());

		}
		return _rules;
	}
	
	public static void genrules(List<String> itemset, List<List<String>> consequente_m, int m, int support) {
		int k = itemset.size();

		//System.out.println("");
		//System.out.println("k:"+k+"  m:"+m);
		//System.out.println(consequente_m);
		if (k > m) {
			Iterator<List<String>> iterator = consequente_m.iterator();
			while(iterator.hasNext()) {
				List<String> consequente = iterator.next();

				List<String> lhs = new ArrayList<String>();
				lhs.addAll(itemset);

				//System.out.println("Frequent Itemset:"+lhs);
				
				for (String item : consequente) {
					lhs.remove(item);
				}

				//System.out.println("lhs:"+lhs);
				//System.out.println("rhs:"+consequente);

				//System.out.println(support);
				float confidence = (float)support/_frequentItemsets.get(lhs);
				//System.out.println(confidence);

				if (confidence >= _minconf) {
					//System.out.println("New rule");
					Rule rule = new Rule(lhs, consequente, support, confidence);
					_rules.add(rule);
				}
				else {
					iterator.remove();
				}

				//System.out.println(consequente_m);
			}

			List<List<String>> consequente_m1 = gen_candidate(consequente_m);
			//System.out.println(consequente_m1);
			m++;
			genrules(itemset, consequente_m1, m, support);
		}
	}

	public static List<List<String>> gen_candidate(List<List<String>> consequente_m) {
		List<List<String>> consequente_m1 = new ArrayList<List<String>>();
		int k = consequente_m.size();
		if (k < 2) {
			return consequente_m1;
		}

		int n = consequente_m.get(0).size();
		for (int i = 0; i < k-1; i++) {
			for (int j = i+1; j < k; j++) {
				// Merge i and j
				List<String> m1 = new ArrayList<String>();
				m1.addAll(consequente_m.get(i));
				List<String> m2 = new ArrayList<String>();
				m2.addAll(consequente_m.get(j));

				int index = 0;
				List<String> temp = new LinkedList<>();
				while (m1.get(index).equals(m2.get(index)) && index < n-1 ){
					temp.add(m1.get(index));
					index++;
				}

				if(index == n-1){
					String a = m1.get(index);
					String b = m2.get(index);
					if (_option == 1) {
						if (Integer.parseInt(a)<=Integer.parseInt(a)) {
							temp.add(a);
							temp.add(b);
						}
						else{
							temp.add(b);
							temp.add(a);
						}
					}
					if (_option == 2) {
						if (_itemsets.get(a)<=_itemsets.get(b)) {
							temp.add(a);
							temp.add(b);
						}
						else{
							temp.add(b);
							temp.add(a);
						}
					}
					if (_option == 3) {
						if (_itemsets.get(a)>_itemsets.get(b)) {
							temp.add(a);
							temp.add(b);
						}
						else{
							temp.add(b);
							temp.add(a);
						}
					}

					consequente_m1.add(temp);
				}
			}
		}
		return consequente_m1;
	}
	
	public static void storeRules() throws IOException {
		String path = _outputfile+"_"+_minsup+"_"+_minconf;
		BufferedWriter bw = new BufferedWriter(new FileWriter(path));
		for (Rule rule : _rules) {
			bw.write(rule.lhs+" "+rule.rhs+" "+rule.support+" "+rule.confidence+"\n");
		}
		bw.close();
	}

	public static void main (String[] args) throws IOException {
		long startTime = System.nanoTime();

		hcrminer miner = new hcrminer(Integer.parseInt(args[0]), Float.parseFloat(args[1]), args[2], args[3], Integer.parseInt(args[4]));

		miner.readfile(_inputfile, _option);

		_FPTree = new FPTree(_minsup, _minconf, _option);
		_FPTree.build(_transactions, null);

		_frequentItemsets= _FPTree.getFrequentItemsets();
		//System.out.println(_frequentItemsets.entrySet());
		System.out.println("No. of Frequent Itemset: "+_frequentItemsets.size());

		long endTime   = System.nanoTime();
		double totalTime = (double)(endTime - startTime) / 1000000000.0;
		System.out.println("Running time: "+totalTime);

		startTime = System.nanoTime();

		generateRules();
		//System.out.println(_rules);
		System.out.println("No. of Rules: "+_rules.size());

		storeRules();

		endTime   = System.nanoTime();
		totalTime = (double)(endTime - startTime) / 1000000000.0;
		System.out.println("Running time: "+totalTime);
	}
}