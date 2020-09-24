The web client and Android application for **Teedy** are only examples
of what is possible with the provided REST API. Everything you see in those apps are
accessible using the API.

This documentation is divided in two parts. The first will get you started on essentials
steps like authentication and the second part is a full reference of every endpoints.

## API URL
The base URL depends on your server. If your instance of Teedy is accessible through
`https://teedy.mycompany.com`, then the base API URL is `https://teedy.mycompany.com/api`.

## Verbs and status codes
The API uses restful verbs.

| Verb | Description |
|---|---|
| `GET` | Select one or more items |
| `PUT` | Create a new item |
| `POST` | Update an item |
| `DELETE` | Delete an item |

Successful calls return a HTTP code 200, anything else if an error.

## Dates
All dates are returned in UNIX timestamp format in milliseconds.

## Authentication
#### **Step 1: [POST /user/login](#api-User-PostUserLogin)**

A call to this endpoint will return a cookie header. Here is a CURL example:
```
curl -i -X POST -d username=admin -d password=admin https://docs.mycompany.com/api/user/login
Set-Cookie: auth_token=64085630-2ae6-415c-9a92-4b22c107eaa4
```

#### **Step 2: Authenticated API calls**

All following API calls must have a cookie header supplying the given token. Here is a CURL example:
```
curl -i -X GET -H "Cookie: auth_token=64085630-2ae6-415c-9a92-4b22c107eaa4" https://docs.mycompany.com/api/document/list
{"total":12,"documents":[...]}
```

#### **Step 3: [POST /user/logout](#api-User-PostUserLogout)**

A call to this API with a given `auth_token` cookie will make it unusable for other calls.
```
curl -i -X POST -H "Cookie: auth_token=64085630-2ae6-415c-9a92-4b22c107eaa4" https://docs.mycompany.com/api/user/logout
```