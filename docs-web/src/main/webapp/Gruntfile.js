module.exports = function(grunt) {

  // Project configuration.
  grunt.initConfig({
    pkg: grunt.file.readJSON('package.json'),
    clean: {
      dist: {
        src: ['dist']
      }
    },
    ngmin: {
      dist: {
        expand: true,
        cwd: 'src',
        src: ['app/**/*.js'],
        dest: 'dist'
      }
    },
    concat: {
      docs: {
        options: {
          separator: ';'
        },
        src: ['src/lib/jquery.js','src/lib/jquery.ui.js','src/lib/underscore.js','src/lib/colorpicker.js', 'src/lib/angular.js', 'src/lib/angular.*.js',
          'dist/app/docs/app.js', 'dist/app/docs/controller/**/*.js', 'dist/app/docs/directive/*.js', 'dist/app/docs/filter/*.js', 'dist/app/docs/service/*.js'],
        dest: 'dist/docs.js'
      },
      share: {
        options: {
          separator: ';'
        },
        src: ['src/lib/jquery.js','src/lib/jquery.ui.js','src/lib/underscore.js','src/lib/colorpicker.js', 'src/lib/angular.js', 'src/lib/angular.*.js',
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
    copy: {
      dist: {
        expand: true,
        cwd: 'src/',
        src: ['**', '!**/*.js', '!*.html', '!**/*.less', '!**/*.css'],
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
    remove: {
      dist: {
        fileList: ['dist/style.css', 'dist/docs.js', 'dist/share.js', 'dist/less.css'],
        dirList: ['dist/app']
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
        src: ['dist/docs.min.js', 'dist/share.min.js', 'dist/**/*.html'],
        overwrite: true,
        replacements: [{
          from: '../api',
          to: grunt.option('apiurl') || '../api'
        }]
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
  grunt.loadNpmTasks('grunt-remove');
  grunt.loadNpmTasks('grunt-ngmin');
  grunt.loadNpmTasks('grunt-text-replace');

  // Default tasks.
  grunt.registerTask('default', ['clean', 'ngmin', 'concat:docs', 'concat:share', 'less', 'concat:css', 'cssmin',
    'uglify:docs', 'uglify:share', 'copy', 'remove', 'cleanempty', 'htmlrefs:index', 'htmlrefs:share', 'replace']);

};