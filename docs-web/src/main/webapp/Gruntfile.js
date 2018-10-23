module.exports = function(grunt) {

  // Project configuration.
  grunt.initConfig({
    pkg: grunt.file.readJSON('package.json'),
    clean: {
      init: ['dist'],
      after: ['dist/style.css', 'dist/docs.js', 'dist/share.js', 'dist/less.css', 'dist/app', 'dist/partial']
    },
    ngAnnotate: {
      options: {
        singleQuotes: true
      },
      dist: {
        files: [{
          expand: true,
          cwd: 'src',
          src: ['app/**/*.js'],
          dest: 'dist'
        }]
      }
    },
    concat: {
      docs: {
        options: {
          separator: ';'
        },
        src: ['src/lib/jquery.js','src/lib/jquery.ui.js','src/lib/underscore.js','src/lib/colorpicker.js', 'src/lib/pell.js', 'src/lib/angular.js', 'src/lib/angular.*.js',
          'dist/app/docs/app.js', 'dist/app/docs/controller/**/*.js', 'dist/app/docs/directive/*.js', 'dist/app/docs/filter/*.js', 'dist/app/docs/service/*.js'],
        dest: 'dist/docs.js'
      },
      share: {
        options: {
          separator: ';'
        },
        src: ['src/lib/jquery.js','src/lib/jquery.ui.js','src/lib/underscore.js','src/lib/colorpicker.js', 'src/lib/pell.js', 'src/lib/angular.js', 'src/lib/angular.*.js',
          'dist/app/share/app.js', 'dist/app/share/controller/*.js', 'dist/app/share/directive/*.js', 'dist/app/share/filter/*.js', 'dist/app/share/service/*.js'],
        dest: 'dist/share.js'
      },
      css: {
        src: ['src/style/*.css', 'dist/less.css'],
        dest: 'dist/style.css'
      }
    },
    less: {
      dist: {
        src: ['src/style/*.less'],
        dest: 'dist/less.css'
      }
    },
    cssmin: {
      dist: {
        src: 'dist/style.css',
        dest: 'dist/style/style.min.css'
      }
    },
    uglify: {
      docs: {
        src: 'dist/docs.js',
        dest: 'dist/docs.min.js'
      },
      share: {
        src: 'dist/share.js',
        dest: 'dist/share.min.js'
      }
    },
    ngtemplates: {
      docs: {
        cwd: 'src',
        src: 'partial/docs/*.html',
        dest: 'dist/docs.min.js',
        options: {
          append: true,
          htmlmin: {
            collapseBooleanAttributes: false,
            collapseWhitespace: true,
            removeAttributeQuotes: false,
            removeComments: true,
            removeEmptyAttributes: false,
            removeRedundantAttributes: false,
            removeScriptTypeAttributes: true,
            removeStyleLinkTypeAttributes: true
          }
        }
      },
      share: {
        cwd: 'src',
        src: 'partial/share/*.html',
        dest: 'dist/share.min.js',
        options: {
          append: true,
          htmlmin: {
            collapseBooleanAttributes: false,
            collapseWhitespace: true,
            removeAttributeQuotes: false,
            removeComments: true,
            removeEmptyAttributes: false,
            removeRedundantAttributes: false,
            removeScriptTypeAttributes: true,
            removeStyleLinkTypeAttributes: true
          }
        }
      }
    },
    copy: {
      dist: {
        expand: true,
        cwd: 'src/',
        src: ['**', '!**/*.js', '!*.html', '!**/*.less', '!**/*.css', 'locale/**'],
        dest: 'dist/'
      }
    },
    htmlrefs: {
      index: {
        src: 'src/index.html',
        dest: 'dist/index.html'
      },
      share: {
        src: 'src/share.html',
        dest: 'dist/share.html'
      }
    },
    cleanempty: {
      options: {
        files: false,
        folders: true
      },
      src: ['dist/**']
    },
    replace: {
      dist: {
        src: ['dist/docs.min.js', 'dist/share.min.js', 'dist/**/*.html', 'dist/style/style.min.css'],
        overwrite: true,
        replacements: [{
          from: '../api',
          to: grunt.option('apiurl') || '../api'
        }, {
          from: '@build.date@',
          to: new Date().getTime()
        }]
      }
    },
    apidoc: {
      generate: {
        src: '../java/',
        dest: 'dist/apidoc/'
      }
    }
  });

  grunt.loadNpmTasks('grunt-contrib-clean');
  grunt.loadNpmTasks('grunt-contrib-concat');
  grunt.loadNpmTasks('grunt-contrib-uglify');
  grunt.loadNpmTasks('grunt-contrib-copy');
  grunt.loadNpmTasks('grunt-cleanempty');
  grunt.loadNpmTasks('grunt-htmlrefs');
  grunt.loadNpmTasks('grunt-css');
  grunt.loadNpmTasks('grunt-contrib-less');
  grunt.loadNpmTasks('grunt-ng-annotate');
  grunt.loadNpmTasks('grunt-text-replace');
  grunt.loadNpmTasks('grunt-apidoc');
  grunt.loadNpmTasks('grunt-angular-templates');

  // Default tasks.
  grunt.registerTask('default', ['clean:init', 'ngAnnotate', 'concat:docs', 'concat:share', 'less', 'concat:css',
    'cssmin', 'uglify:docs', 'uglify:share', 'ngtemplates:docs', 'ngtemplates:share', 'copy', 'clean:after',
    'cleanempty', 'htmlrefs:index', 'htmlrefs:share', 'replace', 'apidoc']);

};