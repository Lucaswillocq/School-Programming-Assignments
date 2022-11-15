Lucas Willocq
CS 1550
Dr. Lee T-Th 925-1040

For this assignment, we were asked to run multiple traces of a file full of memory references on two different page replacement algorithms.  Using LRU and OPT, I used trace.1 to obtain the statistics and graphs included.

From the first boxplot, comparing LRU to OPT on diskwrites and page faults, you can see that OPT resulted in less disk writes and page faults, always.  LRU also had much further outliers, meaning it's performance improves exponentially when page size / number of frames increases, or that edge cases significantly hurt it's runtime.  OPT had a lot less outliers, meaning that regardless of memory allocation or command line arguments, OPT produces a more reliable runtime than LRU.  

![image](https://user-images.githubusercontent.com/70659913/113533624-5a753680-959c-11eb-82f3-b80434846532.png)



When comparing the performance of various memory splits, it's interesting to note that the split that resulted in the most pagefaults and diskwrites was 1:3.  This may have to do with the file I was testing and the random assortment of it's memory accesses.  

![image](https://user-images.githubusercontent.com/70659913/113533715-94ded380-959c-11eb-94e9-697a469d2a8c.png)


When Page size increased from 4 KB to 4 MB, the page faults and disk writes of each algorithm improved significantly.  This is due to our page table containing less entries, meaning more of the addresses we scan in will result in page hits as the individual pages are much larger.  Increasing number of frames also improved performance, as there are more open spots in memory for our processes to work.

![image](https://user-images.githubusercontent.com/70659913/113533940-3d8d3300-959d-11eb-87b3-da72c995fbf4.png)


***I'm aware I was not able to provide very useful graphs.  Ideally I would have liked to be able to display it in the different memory setups, and then within that two groups for each Algorithm.  I could not figure out how to do this in the programs I have on my laptop, so I apologize if these images are not as helpful as they should be. 

