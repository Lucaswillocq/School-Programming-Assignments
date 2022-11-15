#include <linux/syscalls.h>
#include <linux/kernel.h>
#include <linux/uaccess.h>
#include <linux/spinlock.h>
#include <linux/stddef.h>
#include <linux/list.h>
#include <linux/slab.h>
#include <linux/cs1550.h>

/**
 * Creates a new semaphore. The long integer value is used to
 * initialize the semaphore's value.
 *
 * The initial `value` must be greater than or equal to zero.
 *
 * On success, returns the identifier of the created
 * semaphore, which can be used with up() and down().
 *
 * On failure, returns -EINVAL or -ENOMEM, depending on the
 * failure condition.
 */


static DEFINE_RWLOCK(sem_rwlock);
// sem_rwlock = RWlock
static LIST_HEAD(sem_list);
//sem_list = list of semaphores

unsigned long sem_id_gen = 0;
//global that gets incremented upon creation

SYSCALL_DEFINE1(cs1550_create, long, value)
{
	
	if(value<0)
	{return -EINVAL;}
	
	struct cs1550_sem *semaphore = (struct cs1550_sem*)kmalloc(sizeof(struct cs1550_sem),GFP_ATOMIC);
	
	if(!semaphore)
	{return -ENOMEM;}
	
	sem_id_gen = sem_id_gen + 1;
	
	semaphore->value = value;
	semaphore->sem_id = sem_id_gen;
	
	spin_lock_init(&semaphore->lock);
	
	INIT_LIST_HEAD(&semaphore->list);
	INIT_LIST_HEAD(&semaphore->waiting_tasks);
	
	struct cs1550_task *task_node = (struct cs1550_task*)kmalloc(sizeof(struct cs1550_task),GFP_ATOMIC);
	
	if(!task_node)
	{return -ENOMEM;}
	
	INIT_LIST_HEAD(&task_node->list);
	task_node->task = current;
	
	write_lock(&sem_rwlock);
	
	list_add(&semaphore->list, &sem_list);
	
	write_unlock(&sem_rwlock);
	
	return semaphore->sem_id;
}
/**
 * Performs the down() operation on an existing semaphore
 * using the semaphore identifier obtained from a previous call
 * to cs1550_create().
 *
 * This decrements the value of the semaphore, and *may cause* the
 * calling process to sleep (if the semaphore's value goes below 0)
 * until up() is called on the semaphore by another process.
 *
 * Returns 0 when successful, or -EINVAL or -ENOMEM if an error
 * occurred.
 */
SYSCALL_DEFINE1(cs1550_down, long, sem_id)
{
	read_lock(&sem_rwlock);
	
	struct cs1550_sem *searchsemaphore = NULL;
	
	if(list_empty(&sem_list))
	{return -EINVAL;}
	
	struct cs1550_sem *sem = NULL;
	
	list_for_each_entry(sem, &sem_list, list )
	{
		if(sem->sem_id == sem_id)
		{searchsemaphore = sem;}
	}
	
	if(searchsemaphore==NULL)
	{return -EINVAL;}
	
	spin_lock(&searchsemaphore->lock);
	
	searchsemaphore->value = searchsemaphore->value - 1;
	
	if(searchsemaphore->value < 0)
	{
		struct cs1550_task *new_task = (struct cs1550_task *)kmalloc(sizeof(struct cs1550_task), GFP_ATOMIC);
		if(!new_task)
		{return -ENOMEM;}
		
		new_task->task = current;
		
		INIT_LIST_HEAD(&new_task->list);
		
		list_add_tail(&new_task->list, &searchsemaphore->waiting_tasks);
		
		set_current_state(TASK_INTERRUPTIBLE);
		spin_unlock(&searchsemaphore->lock);
		schedule();
		
	}
	else
	{spin_unlock(&searchsemaphore->lock);}
	
	read_unlock(&sem_rwlock);
	
	return 0;
}

/**
 * Performs the up() operation on an existing semaphore
 * using the semaphore identifier obtained from a previous call
 * to cs1550_create().
 *
 * This increments the value of the semaphore, and *may cause* the
 * calling process to wake up a process waiting on the semaphore,
 * if such a process exists in the queue.
 *
 * Returns 0 when successful, or -EINVAL if the semaphore ID is
 * invalid.
 */
SYSCALL_DEFINE1(cs1550_up, long, sem_id)
{
	
	read_lock(&sem_rwlock);
	
	struct cs1550_sem *searchsemaphore = NULL;
	
	if(list_empty(&sem_list))
	{return -EINVAL;}
	
	struct cs1550_sem *sem = NULL;
	
	list_for_each_entry(sem, &sem_list, list)
	{
		if(sem->sem_id == sem_id)
		{searchsemaphore = sem;}
	}
	if(searchsemaphore==NULL)
	{return -EINVAL;}
	
	spin_lock(&searchsemaphore->lock);
	
	searchsemaphore->value = searchsemaphore->value + 1;
	
	if(searchsemaphore->value <=0)
	{
		
		struct cs1550_task *temptask = list_first_entry(&searchsemaphore->waiting_tasks, struct cs1550_task ,list);
		
		list_del(&temptask->list);
		
		wake_up_process(temptask->task);
	}
	
	spin_unlock(&searchsemaphore->lock);
	
	read_unlock(&sem_rwlock);
	
	return 0;
	

}

/**
 * Removes an already-created semaphore from the system-wide
 * semaphore list using the identifier obtained from a previous
 * call to cs1550_create().
 *
 * Returns 0 when successful or -EINVAL if the semaphore ID is
 * invalid or the semaphore's process queue is not empty.
 */
SYSCALL_DEFINE1(cs1550_close, long, sem_id)
{
	write_lock(&sem_rwlock);
	
	struct cs1550_sem *searchsemaphore = NULL;
	
	if(list_empty(&sem_list))
	{return -EINVAL;}
	
	struct cs1550_sem *sem = NULL;
	
	list_for_each_entry(sem, &sem_list, list)
	{
		if(sem->sem_id == sem_id)
		{searchsemaphore = sem;}
	}
	
	if(searchsemaphore==NULL)
	{return -EINVAL;}
	
	spin_lock(&searchsemaphore->lock);
	
	if(list_empty(&searchsemaphore->waiting_tasks))
	{
		list_del(&searchsemaphore->list); 
		
		spin_unlock(&searchsemaphore->lock);
	
		kfree(searchsemaphore);
	}
	else
	{spin_unlock(&searchsemaphore->lock);}
	
	write_unlock(&sem_rwlock);
	
	return 0;
}
	

