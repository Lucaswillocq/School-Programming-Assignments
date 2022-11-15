#include <pthread.h>
#include <stdlib.h>
#include <stdio.h>

#include "museumsim.h"

//
// In all of the definitions below, some code has been provided as an example
// to get you started, but you do not have to use it. You may change anything
// in this file except the function signatures.
//


struct shared_data {
	// Add any relevant synchronization constructs and shared state here.
	// For example:
	   
	     pthread_mutex_t ticket_mutex; //ticket mutex
	     pthread_mutex_t guide_mutex; //mutex for if a guide is ready to enter or not
	    pthread_mutex_t guide_museum_mutex; //mutex for if the museum is ready for guides
	     pthread_mutex_t visitor_mutex; // mutex for if a visitor is ready to enter
	     pthread_mutex_t visitor_museum_mutex; // mutex for if the museum is ready for visitors
	     pthread_mutex_t no_guides_mutex; // mutex to see if there are no guides in the museum
	     pthread_mutex_t no_visitors_mutex; // mutex to see if there are no visitors in the museum
	
	     pthread_cond_t guide_ready; // check if the guide is ready
	     pthread_cond_t guide_museum_ready; // check if the museum is ready for guides
	     pthread_cond_t visitor_ready; // check if the visitor is ready
	     pthread_cond_t visitor_museum_ready; // check if the museum is ready for visitors
	     pthread_cond_t no_guides; // check if there are no guides in the museum
	     pthread_cond_t no_visitors; // check if there are no visitors in the museum
		
	   
	     int tickets;
	     int visitors_in_museum;
	     int visitors_waiting;
	     int guides_in_museum;
	     int guides_done_in_museum; //guides who have finished work, but still in the museum waiting to leave
		
};

static struct shared_data shared;


/**
 * Set up the shared variables for your implementation.
 * 
 * `museum_init` will be called before any threads of the simulation
 * are spawned.
 */
void museum_init(int num_guides, int num_visitors)
{
	shared.tickets = MIN(VISITORS_PER_GUIDE * num_guides, num_visitors);
	
	shared.visitors_waiting = 0;
	
	shared.guides_in_museum = 0;
	
	shared.visitors_in_museum = 0;
	
	shared.guides_done_in_museum = 0;
	
	
	
	pthread_mutex_init(&shared.ticket_mutex, NULL);
	
	pthread_mutex_init(&shared.guide_museum_mutex, NULL);
	
	pthread_mutex_init(&shared.visitor_museum_mutex, NULL);
	
	pthread_mutex_init(&shared.guide_mutex, NULL);
	
	pthread_mutex_init(&shared.visitor_mutex, NULL);
	
	pthread_mutex_init(&shared.no_guides_mutex, NULL);
	
	pthread_mutex_init(&shared.no_visitors_mutex, NULL);
	
	
	
	pthread_cond_init(&shared.guide_ready, NULL);
	
	pthread_cond_init(&shared.guide_museum_ready, NULL);
	
	pthread_cond_init(&shared.visitor_ready, NULL);
	
	pthread_cond_init(&shared.visitor_museum_ready, NULL);
	
	pthread_cond_init(&shared.no_visitors, NULL);
	
	pthread_cond_init(&shared.no_guides, NULL);
	
	//basic initilizations
}


/**
 * Tear down the shared variables for your implementation.
 * 
 * `museum_destroy` will be called after all threads of the simulation
 * are done executing.
 */
void museum_destroy()
{
	pthread_mutex_destroy(&shared.ticket_mutex);
	
	pthread_mutex_destroy(&shared.guide_museum_mutex);
	
	pthread_mutex_destroy(&shared.visitor_museum_mutex);
	
	pthread_mutex_destroy(&shared.guide_mutex);
	
	pthread_mutex_destroy(&shared.visitor_mutex);
	
	pthread_mutex_destroy(&shared.no_visitors_mutex);
	
	pthread_mutex_destroy(&shared.no_guides_mutex);
	
	
	pthread_cond_destroy(&shared.guide_ready);
	
	pthread_cond_destroy(&shared.guide_museum_ready);
	
	pthread_cond_destroy(&shared.visitor_ready);
	
	pthread_cond_destroy(&shared.visitor_museum_ready);
	
	pthread_cond_destroy(&shared.no_visitors);
	
	pthread_cond_destroy(&shared.no_guides);
	
	//basic destroys

	
}


/**
 * Implements the visitor arrival, touring, and leaving sequence.
 */
void visitor(int id)
{
	
	visitor_arrives(id);
	
	pthread_mutex_lock(&shared.ticket_mutex); // protect access to ticket count
	
	if(shared.tickets<1)
	{
		visitor_leaves(id);
		pthread_mutex_unlock(&shared.ticket_mutex);
	}
	else
	{
			shared.tickets--;
		pthread_mutex_unlock(&shared.ticket_mutex);
		
		
		pthread_mutex_lock(&shared.visitor_mutex);
			{shared.visitors_waiting++;}
		pthread_mutex_unlock(&shared.visitor_mutex);
		
		pthread_cond_signal(&shared.visitor_ready);
		
		//visitor gets their ticket, incremements the amount of people in line, and signals that they're ready to enter
		
		pthread_mutex_lock(&shared.visitor_museum_mutex);
		{
			while(shared.visitors_in_museum == GUIDES_ALLOWED_INSIDE* VISITORS_PER_GUIDE)
			{pthread_cond_wait(&shared.visitor_museum_ready, &shared.visitor_museum_mutex);}
		}
		pthread_mutex_unlock(&shared.visitor_museum_mutex);
		
		//visitor waits for museum to be not at full capacity for visitors
		
		pthread_mutex_lock(&shared.guide_mutex);
		{
			while(!shared.guides_in_museum || shared.guides_in_museum == shared.guides_done_in_museum) 
				pthread_cond_wait(&shared.guide_ready, &shared.guide_mutex);
		}
		pthread_mutex_unlock(&shared.guide_mutex);
		
		//visitor waits for a guide, or if there is a guide but they have max people served
		
		pthread_mutex_lock(&shared.visitor_museum_mutex);
		{ shared.visitors_in_museum++;}
		pthread_mutex_unlock(&shared.visitor_museum_mutex);
		
		//at this point the visitor is now in the museum
		
		visitor_tours(id);
		
		visitor_leaves(id);
		
		pthread_mutex_lock(&shared.visitor_museum_mutex);
		{
		shared.visitors_in_museum--;
		}
		pthread_mutex_unlock(&shared.visitor_museum_mutex);
		
		//no longer in museum
		
		
		if(shared.guides_in_museum-shared.guides_done_in_museum > 0 && (shared.visitors_in_museum < GUIDES_ALLOWED_INSIDE* VISITORS_PER_GUIDE))
		   pthread_cond_signal(&shared.visitor_museum_ready);
		   
		//if all guides in the museum aren't done and museum isn't at full capacity, signal that other visitors can enter
		
		 if(shared.visitors_in_museum == 0)
		   pthread_cond_signal(&shared.no_visitors);
		
		//if museum is empty of visitors, signal that it is empty
		
		
	}
		
}	

/**
 * Implements the guide arrival, entering, admitting, and leaving sequence.
 */
void guide(int id)
{
	
		guide_arrives(id);
	
	int visitors_served = 0;
	
	int noguides = 0; //determines if there are no guides in the museum or not
		
			pthread_mutex_lock(&shared.guide_museum_mutex);
			{	
				while(shared.guides_in_museum == GUIDES_ALLOWED_INSIDE)
				pthread_cond_wait(&shared.guide_museum_ready, &shared.guide_museum_mutex);
			}
			pthread_mutex_unlock(&shared.guide_museum_mutex);
	
			//if max guides in museum, guide waits then enters when they can
		
		guide_enters(id);
	
		pthread_mutex_lock(&shared.guide_museum_mutex);
		{
		shared.guides_in_museum++;
		}
		pthread_mutex_unlock(&shared.guide_museum_mutex);
		
		//increment amount of guides in museum
		
		//simulates their time at work, until they are finished
		while(visitors_served!=VISITORS_PER_GUIDE)
		{	
		 	//short circuit the loop if no more tickets or people left waiting
			if(shared.tickets==0 && shared.visitors_waiting==0)
				break;
		
			
		 pthread_mutex_lock(&shared.visitor_mutex);
			{
		 		while(shared.visitors_waiting==0)
					pthread_cond_wait(&shared.visitor_ready, &shared.visitor_mutex);
			}
		pthread_mutex_unlock(&shared.visitor_mutex);
				//wait for a visitor to show up
				
			pthread_mutex_lock(&shared.visitor_mutex);
				{shared.visitors_waiting--;}
			pthread_mutex_unlock(&shared.visitor_mutex);
			
				//decrement amount of visitors in line
			
				pthread_cond_signal(&shared.guide_ready);
				
				//signal that they're ready to take people in
				
				guide_admits(id);
			
				visitors_served++;
	
		}
	
		
		pthread_mutex_lock(&shared.guide_museum_mutex);
		{shared.guides_done_in_museum++;}
		pthread_mutex_unlock(&shared.guide_museum_mutex);
	
		//done with work, increment done workers in museum
	
		pthread_mutex_lock(&shared.visitor_museum_mutex);
		{
		while(shared.visitors_in_museum>0)
		{pthread_cond_wait(&shared.no_visitors,&shared.visitor_museum_mutex);}
		}
		
		//wait for all visitors to clear out
		pthread_mutex_unlock(&shared.visitor_museum_mutex);

	
		if(shared.guides_in_museum==shared.guides_done_in_museum)
		{noguides = 1;}
		
		//check if all guides in museum are done

		pthread_mutex_lock(&shared.guide_museum_mutex);
		{
		while(!noguides)
			pthread_cond_wait(&shared.no_guides,&shared.guide_museum_mutex);
		}
		pthread_mutex_unlock(&shared.guide_museum_mutex);
	
		//wait for all guides to be done
	
		guide_leaves(id);
	
		pthread_mutex_lock(&shared.guide_museum_mutex);
		{
			shared.guides_done_in_museum--;

			shared.guides_in_museum--;
			
		}
		
		//all guides have left museum
		pthread_cond_signal(&shared.no_guides);
	
		pthread_cond_signal(&shared.guide_museum_ready);
		

	
	// guide_enters(id);
	// guide_admits(id);
	// guide_leaves(id);
}
