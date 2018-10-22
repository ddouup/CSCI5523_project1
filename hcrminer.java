import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
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
	public void addChild(TreeNode node) {
		if (getChildNodes() == null) {
			List<TreeNode> list = new ArrayList<TreeNode>();
			list.add(node);
			setChildNodes(list);
		}
		else {
			getChildNodes().add(node);
		}
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

	FPTree(){}

	FPTree(int minsup, float minconf){
		_minsup = minsup;
		_minconf = minconf;
	}

	public void build(LinkedHashMap<Integer, LinkedList<String>> transactions, LinkedHashMap<String, Integer> itemsets) {
		ArrayList<TreeNode> headerTable = buildHeaderTable(itemsets);

		TreeNode root = buildFPTree(transactions, headerTable);

	}

	public ArrayList<TreeNode> buildHeaderTable(LinkedHashMap<String, Integer> itemsets) {
		ArrayList<TreeNode> headerTable = new ArrayList<TreeNode>();
		for (Map.Entry<String, Integer>item : itemsets.entrySet()) {
			//Only store items support count > minsup
			if (item.getValue() > _minsup) {
				System.out.println(item);
				TreeNode node = new TreeNode(item.getKey());
				node.setCount(item.getValue());
				headerTable.add(node);
			}
		}
		return headerTable;
	}

	public TreeNode buildFPTree(LinkedHashMap<Integer, LinkedList<String>> transactions, ArrayList<TreeNode> headerTable) {

		TreeNode root = new TreeNode();
        for (Map.Entry<Integer, LinkedList<String>> unsorted_trans : transactions.entrySet()) {
            LinkedList<String> transaction = transactionSort(unsorted_trans, headerTable);
            /*
            TreeNode subTreeRoot = root;
            TreeNode tmpRoot = null;
            if (root.getChildren() != null) {
                while (!transaction.isEmpty()
                        && (tmpRoot = subTreeRoot.findChild(transaction.peek())) != null) {
                    tmpRoot.countIncrement(1);
                    subTreeRoot = tmpRoot;
                    transaction.poll();
                }
            }
            addNodes(subTreeRoot, record, headerTable);
            */
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
}

public class hcrminer {
	public static int _minsup;
	public static float _minconf;
	public static String _inputfile;
	public static String _outputfile;
	public static int _option;

	//_transactions <id, item item item...>
	public static LinkedHashMap<Integer, LinkedList<String>> _transactions;
	//_itemsets: <item, support_count>
	//compute support count when reading data file
	public static LinkedHashMap<String, Integer> _itemsets;

	public static FPTree _FPTree;

	hcrminer(int minsup, float minconf, String inputfile, String outputfile, int option) throws IOException {
		_minsup = minsup;
		_minconf = minconf;
		_inputfile = inputfile;
		_outputfile = outputfile;
		_option = option;

		_transactions = new LinkedHashMap<Integer, LinkedList<String>>();
		_itemsets = new LinkedHashMap<String, Integer>();

		System.out.println(_minsup);
		System.out.println(_minconf);
		System.out.println(_inputfile);
		System.out.println(_outputfile);
		System.out.println(_option);
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

	public static void main (String[] args) throws IOException {
		long startTime = System.nanoTime();

		hcrminer miner = new hcrminer(Integer.parseInt(args[0]), Float.parseFloat(args[1]), args[2], args[3], Integer.parseInt(args[4]));

		miner.readfile(_inputfile, _option);
		//System.out.println(_itemsets.entrySet());

		_FPTree = new FPTree(_minsup, _minconf);
		_FPTree.build(_transactions, _itemsets);

		//HaspMap<String, Integer> frequent_itemsets = _FPTree.getFrequentItemsets();

		//HaspMap<String, String> rules = _FPTree.getRules();


		long endTime   = System.nanoTime();
		double totalTime = (double)(endTime - startTime) / 1000000000.0;
		System.out.println("Running time: "+totalTime);
	}
}