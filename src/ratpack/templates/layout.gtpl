yieldUnescaped '<!DOCTYPE html>'
html(lang:'en') {
    head {
        meta(charset:'utf-8')
        title(title ?: 'Ratpack Books')
        meta('http-equiv': "Content-Type", content:"text/html; charset=utf-8")
        meta(name: 'viewport', content: 'width=device-width, initial-scale=1.0')
        script(src: '//cdnjs.cloudflare.com/ajax/libs/jquery/2.1.1/jquery.min.js') {}
        script(src: '//maxcdn.bootstrapcdn.com/bootstrap/3.2.0/js/bootstrap.min.js') {}
        link(href: '//maxcdn.bootstrapcdn.com/bootstrap/3.2.0/css/bootstrap.min.css', rel: 'stylesheet')
        link(href: '//maxcdn.bootstrapcdn.com/bootstrap/3.2.0/css/bootstrap-theme.min.css', rel: 'stylesheet')
        link(href: '/styles/style.css', rel: 'stylesheet')
    }
    body {
        div(class:'container') {
            if (msg) {
                div(class: 'alert alert-info alert-dismissable') {
                    button(type: 'button', class: 'close', 'data-dismiss': 'alert', 'aria-hidden':'true', '&times;')
                    yield msg
                }
            }
            bodyContents()
        }
    }
}