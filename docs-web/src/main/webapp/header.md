The web client and Android application for **Sismics Docs** are only examples
of what is possible with the provided REST API. Everything you see in those apps are
accessible using the API.

This documentation is divided in two parts. The first will get you started on essentials
steps like authentication and the second part is a full reference of every endpoints.

## API URL
The base URL depends on your server. If your instance of Docs is accessible through
`https://docs.mycompany.com`, then the base API URL is `https://docs.mycompany.com/api`.

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

A call to this endpoint will return a cookie header like this:
```
HTTP Response:
Set-Cookie: auth_token=64085630-2ae6-415c-9a92-4b22c107eaa4
```

#### **Step 2: Authenticated API calls**

All following API calls must have a cookie header supplying the given token, like this:
```
HTTP Request:
Cookie: auth_token=64085630-2ae6-415c-9a92-4b22c107eaa4
```

#### **Step 3: [POST /user/logout](#api-User-PostUserLogout)**

A call to this API with a given `auth_token` cookie will make it unusable for other calls.
