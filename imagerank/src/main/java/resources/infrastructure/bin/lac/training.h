#ifndef _TRAINING_
#define _TRAINING_

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <set>
#include <map>

#include "itemset.h"
#include "evidence.h"
#include "limits.h"
#include "timer.h"

#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>

using namespace std;

extern map<int, set<int> > TID_CLASSES;
extern int CLASS_MAP[MAX_CLASSES], META_LEARNING, N_TRANSACTIONS, TARGET_ID[MAX_CLASSES], COUNT_TARGET[MAX_CLASSES], CACHE_TRAINING_DATA, ONLY_PROCESS_TRAINING;
extern char *DELIM;
extern map<string, int> CLASS_NAME;
extern map<int, string> SYMBOL_TABLE;
extern map<string, int> ITEM_MAP;
extern set<string> PROC_IDS;

int read_training_set(char* training);

#define TRAINING_CACHE_PATH "./training_cache/"
#define VAR_AUX_FILE TRAINING_CACHE_PATH "VAR_AUX.txt"
#define COUNT_TARGET_FILE TRAINING_CACHE_PATH "COUNT_TARGET.txt"
#define TARGET_ID_FILE TRAINING_CACHE_PATH "TARGET_ID.txt"
#define ITEMSETS_FILE TRAINING_CACHE_PATH "ITEMSETS.txt"
#define TID_CLASSES_FILE TRAINING_CACHE_PATH "TID_CLASSES.txt"
#define CLASS_NAME_FILE TRAINING_CACHE_PATH "CLASS_NAME.txt"
#define SYMBOL_TABLE_FILE TRAINING_CACHE_PATH "SYMBOL_TABLE.txt"
#define ITEM_MAP_FILE TRAINING_CACHE_PATH "ITEM_MAP.txt"

#endif
