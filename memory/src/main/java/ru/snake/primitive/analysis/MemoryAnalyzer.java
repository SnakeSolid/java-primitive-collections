package ru.snake.primitive.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.openjdk.jol.info.GraphLayout;

import ru.snake.primitive.list.IntList;
import ru.snake.primitive.map.IntToIntMap;
import ru.snake.primitive.map.IntToObjectMap;
import ru.snake.primitive.map.ObjectMap;
import ru.snake.primitive.map.ObjectToIntMap;
import ru.snake.primitive.set.IntBitSet;
import ru.snake.primitive.set.IntSet;
import ru.snake.primitive.set.ObjectSet;

/**
 * Measures and compares the memory footprint of primitive collections against
 * their standard JDK equivalents (HashSet, HashMap) using JOL.
 *
 * <p>
 * All collections are populated with the same keys and values so that
 * comparisons reflect structural overhead rather than data differences.
 * </p>
 *
 * <p>
 * Run: {@code java -jar primitive-memory-0.0.1-SNAPSHOT-standalone.jar}
 * </p>
 */
public final class MemoryAnalyzer {

	private static final int[] SIZES = { 10, 100, 1_000, 10_000 };

	private MemoryAnalyzer() {
		// utility
	}

	// ------------------------------------------------------------------
	// Main
	// ------------------------------------------------------------------

	public static void main(String[] args) {
		printHeader();

		for (int size : SIZES) {
			printSizeSeparator(size);
			measureLists(size);
			measureSets(size);
			measureMapsIntInt(size);
			measureMapsIntObject(size);
			measureMapsObjectInt(size);
			measureMapsObjectObject(size);
		}

		printFooter();
	}

	// ------------------------------------------------------------------
	// List measurements
	// ------------------------------------------------------------------

	private static void measureLists(int size) {
		printSection("LISTS");

		List<Integer> values = generateInts(size);

		IntList intList = new IntList();
		for (int v : values) {
			intList.add(v);
		}

		ArrayList<Integer> arrayList = new ArrayList<>(size);
		for (int v : values) {
			arrayList.add(v);
		}

		printCollection("IntList", intList, size);
		printCollection("ArrayList<Integer>", arrayList, size);
		printRow();
	}

	// ------------------------------------------------------------------
	// Set measurements
	// ------------------------------------------------------------------

	private static void measureSets(int size) {
		printSection("SETS");

		// Prepare data
		List<Integer> intKeys = generateInts(size);
		List<String> stringKeys = generateStrings(size);

		// --- Int sets (int keys) ---
		IntBitSet intBitSet = new IntBitSet(size);
		for (int k : intKeys) {
			intBitSet.set(k);
		}

		IntSet intSet = new IntSet();
		for (int k : intKeys) {
			intSet.add(k);
		}

		ObjectSet<Integer> objSetInt = new ObjectSet<>();
		for (int k : intKeys) {
			objSetInt.add(k);
		}

		HashSet<Integer> hashSetInt = new HashSet<>();
		for (int k : intKeys) {
			hashSetInt.add(k);
		}

		printCollection("IntBitSet", intBitSet, size);
		printCollection("IntSet", intSet, size);
		printCollection("ObjectSet<Integer>", objSetInt, size);
		printCollection("HashSet<Integer>", hashSetInt, size);

		printRow();

		// --- Object sets (String keys) ---
		ObjectSet<String> objSetStr = new ObjectSet<>();
		for (String k : stringKeys)
			objSetStr.add(k);

		HashSet<String> hashSetStr = new HashSet<>();
		for (String k : stringKeys) {
			hashSetStr.add(k);
		}

		printCollection("ObjectSet<String>", objSetStr, size);
		printCollection("HashSet<String>", hashSetStr, size);
		printRow();
	}

	// ------------------------------------------------------------------
	// Map measurements
	// ------------------------------------------------------------------

	private static void measureMapsIntInt(int size) {
		printSection("MAPS  int -> int");

		List<Integer> keys = generateInts(size);
		List<Integer> values = generateInts(size);

		IntToIntMap intIntMap = new IntToIntMap();
		HashMap<Integer, Integer> hashMapIntInt = new HashMap<>();

		for (int i = 0; i < size; i++) {
			intIntMap.put(keys.get(i), values.get(i));
			hashMapIntInt.put(keys.get(i), values.get(i));
		}

		printCollection("IntToIntMap", intIntMap, size);
		printCollection("HashMap<Integer,Integer>", hashMapIntInt, size);
		printRow();
	}

	private static void measureMapsIntObject(int size) {
		printSection("MAPS  int -> String");

		List<Integer> keys = generateInts(size);
		List<String> values = generateStrings(size);

		IntToObjectMap<String> intObjMap = new IntToObjectMap<>();
		HashMap<Integer, String> hashMapIntStr = new HashMap<>();

		for (int i = 0; i < size; i++) {
			intObjMap.put(keys.get(i), values.get(i));
			hashMapIntStr.put(keys.get(i), values.get(i));
		}

		printCollection("IntToObjectMap<String>", intObjMap, size);
		printCollection("HashMap<Integer,String>", hashMapIntStr, size);
		printRow();
	}

	private static void measureMapsObjectInt(int size) {
		printSection("MAPS  String -> int");

		List<String> keys = generateStrings(size);
		List<Integer> values = generateInts(size);

		ObjectToIntMap<String> objIntMap = new ObjectToIntMap<>();
		HashMap<String, Integer> hashMapStrInt = new HashMap<>();

		for (int i = 0; i < size; i++) {
			objIntMap.putInt(keys.get(i), values.get(i));
			hashMapStrInt.put(keys.get(i), values.get(i));
		}

		printCollection("ObjectToIntMap<String>", objIntMap, size);
		printCollection("HashMap<String,Integer>", hashMapStrInt, size);
		printRow();
	}

	private static void measureMapsObjectObject(int size) {
		printSection("MAPS  String -> String");

		List<String> keys = generateStrings(size);
		List<String> values = generateStrings(size);

		ObjectMap<String, String> objObjMap = new ObjectMap<>();
		HashMap<String, String> hashMapStrStr = new HashMap<>();

		for (int i = 0; i < size; i++) {
			objObjMap.put(keys.get(i), values.get(i));
			hashMapStrStr.put(keys.get(i), values.get(i));
		}

		printCollection("ObjectMap<String,String>", objObjMap, size);
		printCollection("HashMap<String,String>", hashMapStrStr, size);
		printRow();
	}

	// ------------------------------------------------------------------
	// Measurement helpers
	// ------------------------------------------------------------------

	/**
	 * Measure and print the retained size of a collection instance.
	 */
	private static void printCollection(String name, Object instance, int elementCount) {
		GraphLayout layout = GraphLayout.parseInstance(instance);
		long retained = layout.totalSize();
		System.out.printf("  %-35s  %8d  %8d  %6.2f%n", name, elementCount, retained, (double) retained / elementCount);
	}

	// ------------------------------------------------------------------
	// Data generation — deterministic, reusable
	// ------------------------------------------------------------------

	private static List<Integer> generateInts(int count) {
		List<Integer> list = new ArrayList<>(count);

		for (int i = 0; i < count; i++) {
			list.add(i);
		}

		return list;
	}

	private static List<String> generateStrings(int count) {
		List<String> list = new ArrayList<>(count);

		for (int i = 0; i < count; i++) {
			list.add("key-" + i);
		}

		return list;
	}

	// ------------------------------------------------------------------
	// Formatting
	// ------------------------------------------------------------------

	private static void printHeader() {
		System.out.println("================================================================================");
		System.out.println("  Memory Footprint Analysis — Primitive Collections vs JDK Collections");
		System.out.println("================================================================================");
		System.out.println();
	}

	private static void printSizeSeparator(int size) {
		System.out.println("--------------------------------------------------------------------------------");
		System.out.printf("  Element count: %,d%n", size);
		System.out.println("--------------------------------------------------------------------------------");
		System.out.printf("  %-35s  %8s  %8s  %s%n", "Collection", "Count", "Retained", "Per-element");
		System.out.println("  " + "─".repeat(88));
	}

	private static void printSection(String title) {
		System.out.println("  " + title);
	}

	private static void printRow() {
		System.out.println();
	}

	private static void printFooter() {
		System.out.println("================================================================================");
		System.out.println("  Retained = total bytes reachable from the collection (includes elements)");
		System.out.println("  Per-element = retained / element count (lower is better)");
		System.out.println("================================================================================");
	}
}
