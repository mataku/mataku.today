// Reference: https://blog.a-1.dev/post/2019-04-20-blog-card/

exports.handler = (event, context, callback) => {
  if ('url' in event.queryStringParameters === false) {
    return;
  }

  const url = event.queryStringParameters.url;
  const parser = require("ogp-parser");
  parser(encodeURI(url), true).then(function(data) {
    if (!data.hasOwnProperty('title')) {
      
      if (process.env.HUGO_ENV !== "production") {
        console.error("Error getting ogp data: no ogpData returned");
      }
      return JSON.stringify({});
    } 
    let ogpData = {};
    ogpData['siteName'] = data.title;
    for (let prop in data.ogp) {
      if (/^og:/g.test(prop)) {
        // Avoid overriding value, e.g. use og:image, not og:image:height
        if (prop.split(':')[2] === undefined) {
          ogpData[prop.split(':')[1]] = data.ogp[prop][0];
        }
      }
    }
    for (let prop in data.seo) {
      if (/^og:/g.test(prop)) {
        // Avoid overriding value, e.g. use og:image, not og:image:height
        if (prop.split(':')[2] === undefined) {
          ogpData[prop.split(':')[1]] = data.seo[prop][0];
        }
      }
    }

    if (process.env.HUGO_ENV !== "production") {
      console.log(JSON.stringify(ogpData));
    }
    callback(null, {
      statusCode: 200,
      "headers": { "Content-Type": "application/json; charset=utf-8"},
      body: JSON.stringify(ogpData)
    });
  }).catch(function(error) {
      if (process.env.HUGO_ENV !== "production") {
        console.error(error);
      }
      callback(null, {  
        statusCode: 200,
        "headers": { "Content-Type": "application/json; charset=utf-8"},
        body: JSON.stringify({})
      });
  });

};
