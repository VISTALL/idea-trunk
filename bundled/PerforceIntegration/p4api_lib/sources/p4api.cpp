// p4api.cpp : Defines the entry point for the DLL application.
//

#include "stdafx.h"
#include "p4api.h"

void strOut(messageCallback *cb, char *str) {
	cb(strlen(str), str);
}

#ifdef WIN32
BOOL APIENTRY DllMain( HANDLE hModule, 
                       DWORD  ul_reason_for_call, 
                       LPVOID lpReserved
					 )
{
	switch (ul_reason_for_call)
	{
	case DLL_PROCESS_ATTACH:
	case DLL_THREAD_ATTACH:
	case DLL_THREAD_DETACH:
	case DLL_PROCESS_DETACH:
		break;
	}
    return TRUE;
}
#endif // WIN32

bool disposeConnectionImpl(P4Connection &connection) {
	if (connection.p4_client == 0) return true;

	ClientApi *client = (ClientApi*)connection.p4_client;
	connection.p4_client = 0;
	Error e;
	client->Final(&e);
	if (e.Test()) {
		StrBuf buf;
		e.Fmt(&buf);
		connection.error = strdup(buf.Text());
		delete client;
		return false;
	}
	delete client;
	return true;
}

bool initConnectionImpl(P4Connection &connection) {
	delete (ClientApi*)connection.p4_client;
	connection.p4_client = 0;
	ClientApi *client = new ClientApi();
	Error e;
	if (connection.client != 0) {
		client->SetClient(connection.client);
	}
	if (connection.port != 0) {
		client->SetPort(connection.port);
	}
	if (connection.user != 0) {
		client->SetUser(connection.user);
	}
	if (connection.charset != 0) {
		client->SetCharset(connection.charset);
                int csCode = CharSetApi::Lookup(connection.charset);
                if (csCode != -1) {
                  client->SetTrans(csCode);
                }
	}
	if (connection.password != 0) {
		client->SetPassword(connection.password);
	}
	if (connection.cwd != 0) {
		client->SetCwd(connection.cwd);
	}
	client->Init(&e);
	if (e.Test()) {
		StrBuf buf;
		e.Fmt(&buf);
		connection.error = strdup(buf.Text());
		delete client;
		return false;
	}
	connection.p4_client = client;
	return true;
}

P4API_EXPORT P4Connection createConnection() {
	P4Connection result;
	result.error = 0;
	result.port = 0;
	result.client = 0;
	result.user = 0;
    result.charset = 0;
	result.password = 0;
	result.p4_client = 0;
	result.cwd = 0;
	return result;
}

P4API_EXPORT void initConnection(P4Connection &connection) {
	ClientApi *client = (ClientApi*)connection.p4_client;
	connection.error = 0;
	if (client != 0) {
		if (!disposeConnectionImpl(connection)) return;
	}
	initConnectionImpl(connection);
}

P4API_EXPORT void disposeConnection(P4Connection &connection) {
	connection.error = 0;
	disposeConnectionImpl(connection);
}

class MyClientUser : public ClientUser {
public:
	MyClientUser(messageCallback *out, messageCallback *err) {
		this->out_ = out;
		this->err_ = err;
	}

	virtual ~MyClientUser() {}

	virtual void 	OutputError( const_char *errBuf ) {
		strOut(err_, errBuf);
	}
	virtual void	OutputInfo( char level, const_char *data ) {
		switch( level )
		{
		default:
		case '0': break;
		case '1': strOut(out_, "... "); break;
		case '2': strOut(out_, "... ... "); break;
		}
		strOut(out_, data);
		strOut(out_, "\n");
	}
	virtual void 	OutputBinary( const_char *data, int length ) {
		out_(length, data);
	}
	virtual void 	OutputText( const_char *data, int length ) {
		out_(length, data);
	}
	
private:
	messageCallback *out_;
	messageCallback *err_;
};

P4API_EXPORT void runCommand(P4Connection &connection, char *command, int nargs, char **args, messageCallback *out, messageCallback *err) {
	connection.error = 0;
	MyClientUser ui(out, err);
	ClientApi *client = (ClientApi*)connection.p4_client;
	if (client == 0) {
		printf("client was null - initializing\n");
		if (!initConnectionImpl(connection)) {
			strOut(err, "Failed to initialize client");
			return;
		}
		client = (ClientApi*)connection.p4_client;
	}
	else if (client->Dropped()) {
		printf("client was dropped - initializing\n");
		if (!disposeConnectionImpl(connection)) {
			strOut(err, "Failed to dispose old client");
			return;
		}
		if (!initConnectionImpl(connection)) {
			strOut(err, "Failed to reinitialize client");
			return;
		}
		client = (ClientApi*)connection.p4_client;
	}
	if (connection.client != 0) {
		client->SetClient(connection.client);
	}
	if (connection.user != 0) {
		client->SetUser(connection.user);
	}
	if (connection.charset != 0) {
		client->SetCharset(connection.charset);
	}
	if (connection.password != 0) {
		client->SetPassword(connection.password);
	}
	client->SetArgv(nargs, args);
	client->Run(command, &ui);
}
