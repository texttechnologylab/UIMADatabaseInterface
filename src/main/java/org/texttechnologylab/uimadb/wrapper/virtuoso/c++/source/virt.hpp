#ifndef Virt_H
#define Virt_H

#pragma once
/*
 *  odbc_iri.c
 *
 *  This file is part of the OpenLink Software Virtuoso Open-Source (VOS)
 *  project.
 *
 *  Copyright (C) 1998-2018 OpenLink Software
 *
 *  This project is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the
 *  Free Software Foundation; only version 2 of the License, dated June 1991.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 *
 */

#include <stdio.h>
#include <string.h>
#include <string>
#include <iostream>
#include <tuple>
#include <map>

#ifdef _WIN64
# include <windows.h>
#endif

#include <sql.h>
#include <sqlext.h>

#if defined (HAVE_IODBC)
#include <iodbcext.h>
#endif

 /*
  *  Include Virtuoso ODBC extensions for SPASQL result set
  */
#if !defined (SQL_DESC_COL_DV_TYPE)

  /*
   *  ODBC extensions for SQLGetDescField
   */
# define SQL_DESC_COL_DV_TYPE               1057L
# define SQL_DESC_COL_DT_DT_TYPE            1058L
# define SQL_DESC_COL_LITERAL_ATTR          1059L
# define SQL_DESC_COL_BOX_FLAGS             1060L
# define SQL_DESC_COL_LITERAL_LANG          1061L
# define SQL_DESC_COL_LITERAL_TYPE          1062L

   /*
	*  Virtuoso - ODBC SQL_DESC_COL_DV_TYPE
	*/
# define VIRTUOSO_DV_DATE                   129
# define VIRTUOSO_DV_DATETIME               211
# define VIRTUOSO_DV_DOUBLE_FLOAT           191
# define VIRTUOSO_DV_IRI_ID                 243
# define VIRTUOSO_DV_LONG_INT               189
# define VIRTUOSO_DV_NUMERIC                219
# define VIRTUOSO_DV_RDF                    246
# define VIRTUOSO_DV_SINGLE_FLOAT           190
# define VIRTUOSO_DV_STRING                 182
# define VIRTUOSO_DV_TIME                   210
# define VIRTUOSO_DV_TIMESTAMP              128
# define VIRTUOSO_DV_TIMESTAMP_OBJ          208

	/*
	 *  Virtuoso - ODBC SQL_DESC_COL_DT_DT_TYPE
	 */
# define VIRTUOSO_DT_TYPE_DATETIME          1
# define VIRTUOSO_DT_TYPE_DATE              2
# define VIRTUOSO_DT_TYPE_TIME              3

	 /*
	  *  Virtuoso - ODBC SQL_DESC_COL_BOX_FLAGS
	  */
#define VIRTUOSO_BF_IRI                     0x1
#define VIRTUOSO_BF_UTF8                    0x2
#define VIRTUOSO_BF_DEFAULT_ENC             0x4

#endif




class virt {

	


private:

	SQLHANDLE henv = SQL_NULL_HANDLE;
	SQLHANDLE hdbc = SQL_NULL_HANDLE;
	SQLHANDLE hstmt = SQL_NULL_HANDLE;
	
#define MAXCOLS                             25


public:

	std::wstring dsn{};
	std::wstring uid{};
	std::wstring pwd{};



	virt() {}
	virt(std::wstring &i_dsn, std::wstring &i_uid, std::wstring &i_pwd) {
		dsn = i_dsn;
		uid = i_uid;
		pwd = i_pwd;
	}

	void ODBC_Errors(std::wstring where, std::wstring &sql_err)
	{
		wchar_t buf[250];
		wchar_t sqlstate[15];

		/*
		 *  Get statement errors
		 */
		while (SQLError(henv, hdbc, hstmt, sqlstate, NULL, buf, sizeof(buf), NULL) == SQL_SUCCESS)
		{
			std::wcout << L"STMT: " << where << L" || " << buf << L" ,SQLSTATE = " << sqlstate << std::endl;
			sql_err = buf;
		}

		/*
		 *  Get connection errors
		 */
		while (SQLError(henv, hdbc, SQL_NULL_HSTMT, sqlstate, NULL, buf, sizeof(buf), NULL) == SQL_SUCCESS)
		{
			std::wcout << L"STMT: " << where << L" || " << buf << L" ,SQLSTATE = " << sqlstate << std::endl;
			sql_err = buf;
		}

		/*
		 *  Get environment errors
		 */
		while (SQLError(henv, SQL_NULL_HDBC, SQL_NULL_HSTMT, sqlstate, NULL, buf, sizeof(buf), NULL) == SQL_SUCCESS)
		{
			std::wcout << L"ENV: " << where << L" || " << buf << L" ,SQLSTATE = " << sqlstate << std::endl;
			sql_err = buf;
		}
		sql_err = buf;
	}




	void ODBC_Errors(std::wstring where)
	{
		wchar_t buf[250];
		wchar_t sqlstate[15];

		/*
		 *  Get statement errors
		 */
		while (SQLError(henv, hdbc, hstmt, sqlstate, NULL, buf, sizeof(buf), NULL) == SQL_SUCCESS)
		{
			std::wcout << L"STMT: " << where << L" || " << buf << L" ,SQLSTATE = " << sqlstate << std::endl;
		}

		/*
		 *  Get connection errors
		 */
		while (SQLError(henv, hdbc, SQL_NULL_HSTMT, sqlstate, NULL, buf, sizeof(buf), NULL) == SQL_SUCCESS)
		{
			std::wcout << L"STMT: " << where << L" || " << buf << L" ,SQLSTATE = " << sqlstate << std::endl;
		}

		/*
		 *  Get environment errors
		 */
		while (SQLError(henv, SQL_NULL_HDBC, SQL_NULL_HSTMT, sqlstate, NULL, buf, sizeof(buf), NULL) == SQL_SUCCESS)
		{
			std::wcout << L"ENV: " << where << L" || " << buf << L" ,SQLSTATE = " << sqlstate << std::endl;
		}
	}

	int ODBC_Disconnect(void)
	{
		if (hstmt)
			SQLFreeHandle(SQL_HANDLE_STMT, hstmt);
		hstmt = SQL_NULL_HANDLE;

		if (hdbc)
			SQLDisconnect(hdbc);

		if (hdbc)
			SQLFreeHandle(SQL_HANDLE_DBC, hdbc);
		hdbc = SQL_NULL_HANDLE;

		if (henv)
			SQLFreeHandle(SQL_HANDLE_ENV, henv);
		henv = SQL_NULL_HANDLE;

		return 0;
	}

	int ODBC_Connect(const wchar_t* i_dsn, const wchar_t* i_usr, const wchar_t* i_pwd)
	{
		SQLRETURN rc;

		/* Allocate environment handle */
		rc = SQLAllocHandle(SQL_HANDLE_ENV, SQL_NULL_HANDLE, &henv);
		if (!SQL_SUCCEEDED(rc))
			goto error;

		/* Set the ODBC version environment attribute */
		rc = SQLSetEnvAttr(henv, SQL_ATTR_ODBC_VERSION, (void*)SQL_OV_ODBC3, 0);
		if (!SQL_SUCCEEDED(rc))
			goto error;

		/* Allocate connection handle */
		rc = SQLAllocHandle(SQL_HANDLE_DBC, henv, &hdbc);
		if (!SQL_SUCCEEDED(rc))
			goto error;

		/* Connect to data source */
		rc = SQLConnect(hdbc, (SQLWCHAR*)i_dsn, SQL_NTS, (SQLWCHAR*)i_usr, SQL_NTS, (SQLWCHAR*)i_pwd, SQL_NTS);
		if (!SQL_SUCCEEDED(rc))
			goto error;

		/* Allocate statement handle */
		rc = SQLAllocHandle(SQL_HANDLE_STMT, hdbc, &hstmt);
		if (!SQL_SUCCEEDED(rc))
			goto error;

		/* Successful connection */
		return 0;

	error:
		/* Failed connection */
		std::wstring str = L"ODBC_Connect";

		ODBC_Errors(str);

		ODBC_Disconnect();

		return -1;
	}


	// only if parameters have already beed set
	int ODBC_Connect()
	{
		SQLRETURN rc;

		/* Allocate environment handle */
		rc = SQLAllocHandle(SQL_HANDLE_ENV, SQL_NULL_HANDLE, &henv);
		if (!SQL_SUCCEEDED(rc))
			goto error;

		/* Set the ODBC version environment attribute */
		rc = SQLSetEnvAttr(henv, SQL_ATTR_ODBC_VERSION, (void*)SQL_OV_ODBC3, 0);
		if (!SQL_SUCCEEDED(rc))
			goto error;

		/* Allocate connection handle */
		rc = SQLAllocHandle(SQL_HANDLE_DBC, henv, &hdbc);
		if (!SQL_SUCCEEDED(rc))
			goto error;

		/* Connect to data source */
		rc = SQLConnect(hdbc, &dsn[0], SQL_NTS, &uid[0], SQL_NTS, &pwd[0], SQL_NTS);
		if (!SQL_SUCCEEDED(rc))
			goto error;

		/* Allocate statement handle */
		rc = SQLAllocHandle(SQL_HANDLE_STMT, hdbc, &hstmt);
		if (!SQL_SUCCEEDED(rc))
			goto error;

		/* Successful connection */
		return 0;

	error:
		/* Failed connection */
		std::wstring str = L"ODBC_Connect";

		ODBC_Errors(str);

		ODBC_Disconnect();

		return -1;
	}





	int get_columnsize() {
		SQLRETURN rc;
		short numCols;
		rc = SQLNumResultCols(hstmt, &numCols);
		if (!SQL_SUCCEEDED(rc))
		{
			std::wstring str = L"SQLNumResultCols";
			ODBC_Errors(str);
		}
		return numCols;
	}




	//the map always takes 2 Rows and returns these as a key-Value-Pair. Key = Predicate(RDF), Value = Objedt(RDF)
	//if there is only one column returned its stored in the first.
	int ODBC_ResultToString(std::map<std::wstring, std::wstring> &rdf_map)
	{
		wchar_t fetchBuffer[400000];
		short numCols = 0;
		short colNum;
		SQLLEN colIndicator;
		UDWORD totalRows;
		UDWORD totalSets;
		SQLHANDLE hdesc = SQL_NULL_HANDLE;
		SQLRETURN rc;

		totalSets = 0;
		do
		{
			/*
			 *  Get the number of result columns for this cursor.
			 *  If it is 0, then the statement was probably not a select
			 */
			rc = SQLNumResultCols(hstmt, &numCols);
			if (!SQL_SUCCEEDED(rc))
			{
				std::wstring str = L"SQLNumResultCols";
				ODBC_Errors(str);
				goto endCursor;
			}

			if (numCols == 0)
			{
				std::wcout << L"Statement executed."  << std::endl;
				goto endCursor;
			}
			if (numCols > MAXCOLS)
				numCols = MAXCOLS;

			/*
			 *  Print all the fields
			 */
			totalRows = 0;
			while (1)
			{
				/*
				 *  Fetch next record
				 */
				rc = SQLFetch(hstmt);
				if (rc == SQL_NO_DATA_FOUND)
					break;
				if (!SQL_SUCCEEDED(rc))
				{
					std::wstring str = L"Fetch";
					ODBC_Errors(str);
					break;
				}

				std::wstring first = L"";
				std::wstring second = L"";

				for (colNum = 1; colNum <= numCols; colNum++)
				{
					wchar_t buf[1000];
					SQLINTEGER len;
					int flag, dvtype;
					rdf triple;

					/*
					 *  Fetch this column as character
					 */
					rc = SQLGetData(hstmt, colNum, SQL_C_WCHAR, &fetchBuffer, sizeof(fetchBuffer), &colIndicator);
					if (!SQL_SUCCEEDED(rc))
					{
						std::wstring str = L"SQLGetData";
						ODBC_Errors(str);
						goto endCursor;
					}

					/*
					 *  Get descriptor handle for this statement
					 */
					rc = SQLGetStmtAttr(hstmt, SQL_ATTR_IMP_ROW_DESC, &hdesc, SQL_IS_POINTER, NULL);
					if (!SQL_SUCCEEDED(rc))
					{
						std::wstring str = L"SQLGetStmtAttr";
						ODBC_Errors(str);
						goto endCursor;
					}

					/*
					 *  Get data type of column
					 */
					rc = SQLGetDescField(hdesc, colNum, SQL_DESC_COL_DV_TYPE, &dvtype, SQL_IS_INTEGER, NULL);
					if (!SQL_SUCCEEDED(rc))
					{
						std::wstring str = L"SQLGetDescField";
						ODBC_Errors(str);
						goto endCursor;
					}

					/*
					 *  Get flags
					 */
					rc = SQLGetDescField(hdesc, colNum, SQL_DESC_COL_BOX_FLAGS, &flag, SQL_IS_INTEGER, NULL);
					if (!SQL_SUCCEEDED(rc))
					{
						std::wstring str = L"SQLGetDescField";
						ODBC_Errors(str);
						goto endCursor;
					}

					/*
					 *  Show NULL fields as ****
					 */
					if (colIndicator == SQL_NULL_DATA)
					{
						std::wcout << L"NULL" << std::endl;
					}
					else
					{
						if (flag & VIRTUOSO_BF_IRI){
							(colNum == 1) ? first = fetchBuffer : second = fetchBuffer;
						}
						else if (dvtype == VIRTUOSO_DV_STRING || dvtype == VIRTUOSO_DV_RDF) {
							(colNum == 1) ? first = fetchBuffer : second = fetchBuffer;
						}
						else {
							(colNum == 1) ? first = fetchBuffer : second = fetchBuffer;
						}
						if (dvtype == VIRTUOSO_DV_RDF)
						{
							rc = SQLGetDescField(hdesc, colNum, SQL_DESC_COL_LITERAL_LANG, buf, sizeof(buf), &len);
							if (!SQL_SUCCEEDED(rc))
							{
								std::wstring str = L"SQLGetDescField";
								ODBC_Errors(str);
								goto endCursor;
							}
							if (len)
								(colNum == 1) ? first = fetchBuffer : second = fetchBuffer;

							rc = SQLGetDescField(hdesc, colNum, SQL_DESC_COL_LITERAL_TYPE, buf, sizeof(buf), &len);
							if (!SQL_SUCCEEDED(rc))
							{
								std::wstring str = L"SQLGetDescField";
								ODBC_Errors(str);
								goto endCursor;
							}
							if (len)
								(colNum == 1) ? first = fetchBuffer : second = fetchBuffer;
						}

						//if (colNum < numCols)
						//	putchar(' ');
					}
				}
				rdf_map[first] = second;
				totalRows++;
			}

			totalSets++;
		} while (SQLMoreResults(hstmt) == SQL_SUCCESS);

	endCursor:
		SQLCloseCursor(hstmt);

		return 0;
	}

	int
		ODBC_PrintResult()
	{
		wchar_t fetchBuffer[1000];
		short numCols = 0;
		short colNum;
		SQLLEN colIndicator;
		UDWORD totalRows;
		UDWORD totalSets;
		SQLHANDLE hdesc = SQL_NULL_HANDLE;
		SQLRETURN rc;

		totalSets = 0;
		do
		{
			/*
			 *  Get the number of result columns for this cursor.
			 *  If it is 0, then the statement was probably not a select
			 */
			rc = SQLNumResultCols(hstmt, &numCols);
			if (!SQL_SUCCEEDED(rc))
			{
				std::wstring str = L"SQLNumResultCols";
				ODBC_Errors(str);
				goto endCursor;
			}
			if (numCols == 0)
			{
				std::wcout << L"Statement executed." << std::endl;
				goto endCursor;
			}
			if (numCols > MAXCOLS)
				numCols = MAXCOLS;

			/*
			 *  Print all the fields
			 */
			totalRows = 0;
			while (1)
			{
				/*
				 *  Fetch next record
				 */
				rc = SQLFetch(hstmt);
				if (rc == SQL_NO_DATA_FOUND)
					break;
				if (!SQL_SUCCEEDED(rc))
				{
					std::wstring str = L"Fetch";
					ODBC_Errors(str);
					break;
				}

				for (colNum = 1; colNum <= numCols; colNum++)
				{
					wchar_t buf[1000];
					SQLINTEGER len;
					int flag, dvtype;

					/*
					 *  Fetch this column as character
					 */
					rc = SQLGetData(hstmt, colNum, SQL_C_WCHAR, fetchBuffer, sizeof(fetchBuffer), &colIndicator);
					if (!SQL_SUCCEEDED(rc))
					{
						std::wstring str = L"SQLGetData";
						ODBC_Errors(str);
						goto endCursor;
					}

					/*
					 *  Get descriptor handle for this statement
					 */
					rc = SQLGetStmtAttr(hstmt, SQL_ATTR_IMP_ROW_DESC, &hdesc, SQL_IS_POINTER, NULL);
					if (!SQL_SUCCEEDED(rc))
					{
						std::wstring str = L"SQLGetStmtAttr";
						ODBC_Errors(str);
						goto endCursor;
					}

					/*
					 *  Get data type of column
					 */
					rc = SQLGetDescField(hdesc, colNum, SQL_DESC_COL_DV_TYPE, &dvtype, SQL_IS_INTEGER, NULL);
					if (!SQL_SUCCEEDED(rc))
					{
						std::wstring str = L"SQLGetDescField";
						ODBC_Errors(str);
						goto endCursor;
					}

					/*
					 *  Get flags
					 */
					rc = SQLGetDescField(hdesc, colNum, SQL_DESC_COL_BOX_FLAGS, &flag, SQL_IS_INTEGER, NULL);
					if (!SQL_SUCCEEDED(rc))
					{
						std::wstring str = L"SQLGetDescField";
						ODBC_Errors(str);
						goto endCursor;
					}

					/*
					 *  Show NULL fields as ****
					 */
					if (colIndicator == SQL_NULL_DATA)
					{
						printf("NULL");
					}
					else
					{
						if (flag & VIRTUOSO_BF_IRI)
							std::wcout << fetchBuffer << std::endl; /* IRI */
						else if (dvtype == VIRTUOSO_DV_STRING || dvtype == VIRTUOSO_DV_RDF)
							std::wcout << LR"(\)" << fetchBuffer << LR"(\)" << std::endl; /* literal string */
						else
							std::wcout << fetchBuffer << std::endl; /* value */

						if (dvtype == VIRTUOSO_DV_RDF)
						{
							rc = SQLGetDescField(hdesc, colNum, SQL_DESC_COL_LITERAL_LANG, buf, sizeof(buf), &len);
							if (!SQL_SUCCEEDED(rc))
							{
								std::wstring str = L"SQLGetDescField";
								ODBC_Errors(str);
								goto endCursor;
							}
							if (len)
								std::wcout << (int)len << L"." << buf;

							rc = SQLGetDescField(hdesc, colNum, SQL_DESC_COL_LITERAL_TYPE, buf, sizeof(buf), &len);
							if (!SQL_SUCCEEDED(rc))
							{
								std::wstring str = L"SQLGetDescField";
								ODBC_Errors(str);
								goto endCursor;
							}
							if (len)
								std::wcout << L"^^" << (int)len << L"." << buf << std::endl;
						}

						if (colNum < numCols)
							putchar(' ');
					}
				}
				printf(" .\n");
				totalRows++;
			}

			std::wcout << std::endl << std::endl << "Statement returned " << totalRows << L" Rows." << std::endl;
			totalSets++;
		} while (SQLMoreResults(hstmt) == SQL_SUCCESS);

	endCursor:
		SQLCloseCursor(hstmt);

		return 0;
	}




	int
		ODBC_Execute(const wchar_t *qr)
	{
		int rc;

		if ((rc = SQLExecDirect(hstmt, (SQLWCHAR *)qr, SQL_NTSL)) != SQL_SUCCESS)
		{
			std::wstring str = L"ODBC_Execute";
			ODBC_Errors(str);
			if (rc != SQL_SUCCESS_WITH_INFO)
				return -1;
		}
		return 0;
	}

};

#endif