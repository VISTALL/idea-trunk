#ifdef P4API_EXPORTS
#define P4API_EXPORT extern "C" __declspec(dllexport)
#else
#define P4API_EXPORT extern "C"
#endif

struct P4Connection {
	char *port;
	char *client;
	char *user;
	char *password;
	char *charset;
	char *cwd;
	void *p4_client;
	char *error;
}
#ifdef __GNUC__
__attribute__ ((aligned(8)))
#endif //__GNUC__
;

typedef void messageCallback(int, char *);
