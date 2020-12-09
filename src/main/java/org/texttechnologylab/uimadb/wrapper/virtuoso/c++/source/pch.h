// Tipps für den Einstieg: 
//   1. Verwenden Sie das Projektmappen-Explorer-Fenster zum Hinzufügen/Verwalten von Dateien.
//   2. Verwenden Sie das Team Explorer-Fenster zum Herstellen einer Verbindung mit der Quellcodeverwaltung.
//   3. Verwenden Sie das Ausgabefenster, um die Buildausgabe und andere Nachrichten anzuzeigen.
//   4. Verwenden Sie das Fenster "Fehlerliste", um Fehler anzuzeigen.
//   5. Wechseln Sie zu "Projekt" > "Neues Element hinzufügen", um neue Codedateien zu erstellen, bzw. zu "Projekt" > "Vorhandenes Element hinzufügen", um dem Projekt vorhandene Codedateien hinzuzufügen.
//   6. Um dieses Projekt später erneut zu öffnen, wechseln Sie zu "Datei" > "Öffnen" > "Projekt", und wählen Sie die SLN-Datei aus.

#ifndef PCH_H
#define PCH_H

#include <iostream>
#include <fstream>
#include <string>
#include <vector>
#include <queue>
#include <sstream>
#include <thread>
#include <codecvt>
#include <locale>
#include <chrono>
#include <ctime>
#include <unordered_map>
#include <locale>
#include <codecvt>
#include <algorithm>
#include <cctype>
#include <stack>

#include "rapidxml.hpp"
#include "rapidxml_iterators.hpp"
#include "rapidxml_print.hpp"
#include "rapidxml_utils.hpp"
#include "xml_writer.hpp"


//#include "uima\api.hpp"

#ifdef _WIN64
# include <windows.h>
#endif

//#import <msxml6.dll> raw_interfaces_only
//namespace xml = MSXML2;


//#include <sql.h>
//#include <sqlext.h>

#pragma comment(lib, "odbc32.lib")
/* and / or*/
#pragma comment(lib, "odbccp32.lib")

#if defined (HAVE_IODBC)
#include <iodbcext.h>
#endif


//Typedefinitionen
typedef std::tuple<std::wstring, std::wstring, std::wstring> rdf;
typedef std::vector<rdf> rdf_vector;


#endif //PCH_H
