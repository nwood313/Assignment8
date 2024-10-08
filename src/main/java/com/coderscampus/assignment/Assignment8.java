package com.coderscampus.assignment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Assignment8 {
    private List<Integer> numbers = null;
    private AtomicInteger i = new AtomicInteger(0);

    public Assignment8() {
        try {
            // Make sure you download the output.txt file for Assignment 8
            // and place the file in the root of your Java project
            numbers = Files.readAllLines(Paths.get("output.txt"))
                    .stream()
                    .map(n -> Integer.parseInt(n))
                    .collect(Collectors.toList());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method will return the numbers that you'll need to process from the list
     * of Integers. However, it can only return 1000 records at a time. You will
     * need to call this method 1,000 times in order to retrieve all 1,000,000
     * numbers from the list
     *
     * @return Integers from the parsed txt file, 1,000 numbers at a time
     */
    public List<Integer> getNumbers() {
        int start, end;
        synchronized (i) {
            start = i.get();
            end = Math.min(i.addAndGet(10000), numbers.size()); // adjust end index

            System.out.println("Starting to fetch records " + start + " to " + (end));
        }
        // force thread to pause for half a second to simulate actual Http / API traffic
        // delay
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        }

        List<Integer> newList = new ArrayList<>();
        IntStream.range(start, end)
                .forEach(n -> {
                    newList.add(numbers.get(n));
                });
        System.out.println("Done Fetching records " + start + " to " + (end));
        return newList;
    }

    public void countUniqueNumbers(List<Integer> numbers) {
        Map<Integer, Integer> countMap = new HashMap<>();

        for (Integer number : numbers) {
            countMap.put(number, countMap.getOrDefault(number, 0) + 1);
        }

        for (Map.Entry<Integer, Integer> entry : countMap.entrySet()) {
            System.out.println(entry.getKey() + "=" + entry.getValue());
        }
    }

    public static void main(String[] args) {
        Assignment8 assignment = new Assignment8();
        List<CompletableFuture<List<Integer>>> tasks = new ArrayList<>();
        ExecutorService pool = Executors.newCachedThreadPool();
        for (int i = 0; i <= 1000; i++) {
            CompletableFuture<List<Integer>> task =
                    CompletableFuture.supplyAsync(() -> assignment.getNumbers(), pool);
            tasks.add(task);
        }

        pool.shutdown();

        CompletableFuture<Void> allTasks = CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0]));
        allTasks.thenRun(() -> {
            List<Integer> allNumbers = new ArrayList<>();
            for (CompletableFuture<List<Integer>> task : tasks) {
                try {
                    List<Integer> numbersList = task.get();
                    allNumbers.addAll(numbersList);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            assignment.countUniqueNumbers(allNumbers);
        });
    }
}