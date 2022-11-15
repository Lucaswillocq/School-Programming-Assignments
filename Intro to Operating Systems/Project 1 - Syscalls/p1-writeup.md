 Explain the pros and cons of using a FIFO queue (as compared to a hash table) for the global semaphore list.
 
 
 When using a FIFO queue for the global semaphore list, the primary pro is that adding / removing semaphores is in O(1) time.
 This allows us to easily manipulate the ends of the queue, whether it be removing the head (first in line) or adding to the back (last in line).
 The queue approach is also memory efficient, as it dynamically allocates memory whenever we add a semaphore, and frees memory when we remove an entry.
 There is no overhead for the queue data structure, as it is simply constructed with several individual list head elements.
 If we want to access a sempahore that is not at the very front of the queue however, it becomes complicated.  We must traverse
 the queue which takes O(n) time, and becomes costly as more sempahores are added to the queue.  
 If we had decided to implement a hash table as the global sempahore list, we wouldn't suffer any time losses in adding / removing entries,
 as this is accomplished in O(1) time.  Hash tables also allow us to easily search for a given semaphore within the list, regardless of it's position in the list.
 The hash tables fail to give us a sense of a "line" as the queue's do, meaning we must implement another strategy to keep track of the positions of the sempahores.
 Hash tables also come with an initial overhead of memory, regardless of how many entries are in the data structure.
 
 
