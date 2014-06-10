CC=g++
CFLAGS=-pipe -O2 -fPIC -D_GNU_SOURCE -DOS_LINUX
P4HOME=$(HOME)/p4api.pic
# LFLAGS=-shared -fPIC -L$(HOME)/p4api -Wl,--whole-archive,$(HOME)/p4api/libclient.a,$(HOME)/p4api/librpc.a,$(HOME)/p4api/libsupp.a  -Wl,--no-whole-archive $(HOME)/p4api/ctype.o
LFLAGS=-shared -fPIC -L$(P4HOME) -Wl,--whole-archive,$(P4HOME)/libclient.a,$(P4HOME)/librpc.a,$(P4HOME)/libsupp.a  -Wl,--no-whole-archive
JAVA_PATH=/usr/java/j2sdk1.4.2_04
INCLUDE=-I- -I. -I$(P4HOME)
LIBS=#-lclient -lrpc -lsupp
AR=ar cqs
RANLIB=ranlib
RM=rm -f

FILES=p4api.cpp

OBJS=$(addsuffix .o, $(basename $(FILES)))
OUT=Release/libp4api.so
OUT_LIB=$(addsuffix .a, $(basename $(OUT)))

all: $(OUT)

$(OUT): $(OBJS) Makefile
	$(CC) $(LFLAGS) $(LIBS) $(OBJS) -o $(OUT)

$(OBJS): %.o: %.cpp Makefile
	$(CC) -c $(CFLAGS) $(INCLUDE) $< -o $@

clean:
	$(RM) $(OUT) $(OUT.a)
	$(RM) $(ASMS) $(OBJS)

