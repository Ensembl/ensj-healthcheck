#!/usr/bin/env perl
# Copyright [2017] EMBL-European Bioinformatics Institute
# 
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
#      http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.



=head1 CONTACT

  Please email comments or questions to the public Ensembl
  developers list at <http://lists.ensembl.org/mailman/listinfo/dev>.

  Questions may also be sent to the Ensembl help desk at
  <helpdesk.org>.

=cut


# Script to convert the healthcheck reports written in simple text file into an HTML file.

use strict;
use warnings;
use Getopt::Long;

# Print the usage instructions if run without parameters
usage() unless (scalar(@ARGV));

my ($input_file,$output_file,$version,$help);

GetOptions(
  'i=s'         => \$input_file,
  'o=s'         => \$output_file,
  'v=i'         => \$version,
  'help!'       => \$help,
);

usage("Input and output files must be specified, using the options '-o' and '-i'") unless ($input_file && $output_file);
usage("A release version must be specified, using the option '-v'") unless ($version);

my %test_case_groups;
my %test_cases;
my %status_by_db;
my $results_by_test_case = 0;
my $current_test_case;
my $current_db_name;


my $bt_id_suffix  = 'button';
my $div_id_suffix = 'content';
my $hc_success = 'PASSED';
my $hc_failed  = 'FAILED';
my %status_colour = ( $hc_success => 'label-success',
                      $hc_failed  => 'label-danger',
                      'default'   => 'label-info'
                    );

my $pre_colour = 'pre_colour';
my $new_colour = 'new_colour';
my $err_colour = 'err_colour';
my $span_open_tag = '<span class="bold_font ';

my @key_words = ('from', 'seq_region', 'entries in', 'in variation set', 'consequence type', 'variations having at least one evidence annotation');
my $row_id = 1;

open HC, "< $input_file" or die $!;
while (<HC>) {
  chomp $_;
  next if ($_ eq '' || $_ =~ /^\./);
  
  # Test case results
  if ($results_by_test_case) {
    last if ($_ =~ /^--/);
    # Test case header
    if ($_ =~ /^org.ensembl.healthcheck/) {
      my $tc_lib = (split(' ',$_))[0];
      my @tc_path = split(/\./, $tc_lib);
      $current_test_case = $tc_path[$#tc_path];
      
      # Extra generic test case from production:
      if ($current_db_name && $current_test_case eq 'TestRunnerSelfCheck') {
        $test_case_groups{$current_db_name}{'TestRunnerSelfCheck'} = 'FAILED';
      }
    }
    # Test case detailled row
    else {
      my @tc_result = split(': ', $_, 2);
      my $db_name = $tc_result[0];
         $db_name =~ s/ //g;
      my $tc_res;
      if ($db_name =~ /^\w+_\w+_variation_$version\_/) {
        $current_db_name = $db_name;
        $tc_res = $tc_result[1];
      }
      else {
        $db_name = $current_db_name;
        $tc_res = $_;
        $tc_res =~ s/^\s+//;
      }
      
      ## Compare versions ##
      my ($previous_count,$new_count,$item,$feature,$short_report);
      # New count
      if ($tc_res =~ /has none/) {
        $new_count = 'none';
      }
      elsif ($tc_res =~ /only has ([0-9]+)/) {
        $new_count = $1;
      }
      
      # Previous count + feature + item
      if ($tc_res =~ /has ([0-9]+) number of /) {
        $previous_count = $1;
        foreach my $key_word (@key_words) {
          if ($tc_res =~ /number of (.+) $key_word (.+) but/) {
            $feature = $1;
            $item = $2;
          }
          elsif ($tc_res =~ /number of $key_word (.+) but/) {
            $feature = $key_word;
            $item = $1;
          }
        }
      }
      elsif ($tc_res =~ /has ([0-9]+) /) {
        $previous_count = $1;
        foreach my $key_word (@key_words) {
          if ($tc_res =~ /has [0-9]+ $key_word (.+) but/) {
            $feature = $key_word;
            $item = $1;
          }
        }
      }
      
      # Formatting short report row
      if ($previous_count && $new_count && $item && $feature) {
        $previous_count = thousandify($previous_count);
        $previous_count =~ s/([0-9]+)/$span_open_tag$pre_colour"\>$1\<\/span\>/g;
        $new_count = thousandify($new_count);
        $new_count      =~ s/([0-9]+|none)/$span_open_tag$new_colour"\>$1\<\/span\>/g;
        $item           = "<b>$item</b>";
        $short_report = 1;
      }
      
      # General text decorators
      $tc_res =~ s/has none/has $span_open_tag$new_colour"\>none\<\/span\>/;
      $tc_res =~ s/FAILURE DETAILS: ([0-9]+)/FAILURE DETAILS: $span_open_tag$err_colour"\>$1\<\/span\>/;
      $tc_res =~ s/has ([0-9]+) /has $span_open_tag$pre_colour"\>$1\<\/span\> /;
      $tc_res =~ s/only has ([0-9]+)/only has $span_open_tag$new_colour"\>$1\<\/span\>/;
      $tc_res =~ s/^([0-9]+)/$span_open_tag$err_colour"\>$1\<\/span\>/;
      $tc_res = thousandify($tc_res);
      $tc_res =~ s/Table `(.+)` exists/Table \<b\>`$1`\<\/b\> exists/g;
      foreach my $item (@key_words) {
        $tc_res =~ s/$item (.+) but/$item \<b\>$1\<\/b\> but/g;
      }
      
      # Generate row reports
      if ($short_report) {
        my $div_id = lc($current_test_case)."_".$row_id;
        $row_id ++;
           
        $tc_res = qq{
          <div>
            <button id="$div_id\_$bt_id_suffix" class="glyphicon glyphicon-plus-sign showhide_hc" onclick="showhide_row('$div_id')"></button> 
            <span>$feature $item : $previous_count / $new_count</span>
          </div>
          <div id="$div_id\_$div_id_suffix" class="hc_extra_row_div" style="display:none">
            $tc_res
          </div>
        };
      }
      
      # Add row reports
      if ($test_cases{$db_name}{$current_test_case}) {
        push(@{$test_cases{$db_name}{$current_test_case}},$tc_res);
      }
      else {
        $test_cases{$db_name}{$current_test_case} = [$tc_res];
      }
    }
  }
  # Test case summary results (i.e. top table)
  else {
    if ($_ =~ /RESULTS BY TEST CASE/) {
      $results_by_test_case = 1;
      next;
    }
    my ($test_case,$db_name,$status);
    if ($_ =~ /\[/) {
      ($test_case,$db_name,$status) = $_ =~ /^(\w+)\s\[(.+)\]\s+\.*\s?(\w+)$/;
      $test_case_groups{$db_name}{$test_case} = $status;
      $current_db_name = $db_name;
    }
    else {
      ($test_case,$status) = $_ =~ /^(\w+)\s\.*\s?(\w+)$/;
      $test_case_groups{$current_db_name}{$test_case} = $status;
    }
    
    $status_by_db{$current_db_name}{$status} ++;
  }
}
close(HC);



#################
#  Output file  #
#################

my $head_html = get_html_head();

open RESULTS, "> $output_file" or die $!;
print RESULTS qq{
<html>$head_html
  <body style="padding: 5px 8px">
    <a id="top"></a>
};

foreach my $db_name (sort(keys(%test_case_groups))) {
  # Species header
  my $status_html = '';
  foreach my $status (sort{ ($a ne $hc_success) cmp ($b ne $hc_success) || $a cmp $b } keys(%{$status_by_db{$db_name}})) {
    my $status_c = $status_colour{$status} ? $status_colour{$status} : $status_colour{'default'};
    #$status_html .= '<span class="hc_font_1"> | </span>' if ($status_html ne '');
    #my $hc_status = ($status_html eq '') ? ' hc_status' : '';
    $status_html .= sprintf( qq{<span class="label %s hc_status hc_font_1"><span class="black">%i</span> %s</span>},
                             $status_c, $status_by_db{$db_name}{$status}, $status
                           );
  }
  print RESULTS qq{
    <div class="db_header">
      <button id="$db_name\_button" class="glyphicon glyphicon-menu-down showhide_hc" onclick="showhide('$db_name')"></button>
      <span>$db_name</span>$status_html
    </div>
    <div id="$db_name\_content">
      <div>\n};
  
  # Passed successfully healtChecks list
  if ($status_by_db{$db_name}{$hc_success}) {
    print RESULTS get_hc_list($db_name, $hc_success);
  }
  
  # Other healtChecks list
  if ($status_by_db{$db_name}{$hc_failed}) {
    print RESULTS get_hc_list($db_name, $hc_failed);
  }
  
  # Colour legend
  print RESULTS qq{
        <div class="legend_box">
          <div class="bold_font">Colour legend</div>
          <div><span class="$pre_colour">123456</span><span>: number of entries in the previous release</span></div>
          <div><span class="$new_colour">123456</span><span>: number of entries in the new release</span></div>
          <div><span class="$err_colour">123456</span><span>: number of entries in the error report</span></div>
        </div>
        <div style="clear:both"></div>
      </div>
  };
  
  # HealtChecks results
  foreach my $tc (sort(keys(%{$test_case_groups{$db_name}}))) {
    my $status = $test_case_groups{$db_name}{$tc};
    next if ($status eq $hc_success);
    my $status_c = $status_colour{$status} ? $status_colour{$status} : $status_colour{'default'};
    my $tc_label = $tc;
       $tc_label =~ s/([A-Z])/ $1/g;
       $tc_label =~ s/My S Q L/MySQL/;
    my $tc_id = lc($tc);
    my $button_id = "$tc_id\_$bt_id_suffix";

    if ($test_cases{$db_name}{$tc} && scalar(@{$test_cases{$db_name}{$tc}})) {
      print RESULTS sprintf(
        qq{
    <div id="%s" class="hc_header">
      <button id="%s" class="glyphicon glyphicon-menu-down showhide_hc" onclick="showhide('%s')"></button>
     %s<span class="label %s hc_status hc_font_1">%s</span>
      <div class="hc_top_link hc_font_1"><a href="#top"><span class="glyphicon glyphicon-circle-arrow-up"></span> top</a></div>
    </div>\n},
        $tc_id, $button_id, $tc_id, $tc_label, $status_c, $status
      );
    
      my $content_id = "$tc_id\_$div_id_suffix";
      print RESULTS qq{    <div id="$content_id">
      <table class="table table-hover hc_font_1"><tbody>\n};
      foreach my $result (@{$test_cases{$db_name}{$tc}}) {
        print RESULTS qq{        <tr><td>$result</td></tr>\n};
      }
      print RESULTS qq{      </tbody></table>\n    </div>\n};
    }
  }
  print RESULTS qq{</div>};
}
print RESULTS qq{</body></html>};


sub get_hc_list {
  my $db_name = shift;
  my $type    = shift;

  my $html = '';

  my $count_col_header = '';
  my $margin_left      = '';
  if ($type ne $hc_success) {
    $count_col_header = qq{\n                <th>Count</th>};
    $margin_left = ';margin-left:30px' if ($status_by_db{$db_name}{$hc_success});
  }
  $html .=  qq{
        <div style="float:left$margin_left">
          <table class="table table-hover table_hc_list">
            <thead>
              <tr>
                <th>HealthCheck $hc_failed</th>
                <th>Status</th>$count_col_header
              </tr>
            </thead>
            <tbody>\n};
            
  foreach my $tc (sort(keys(%{$test_case_groups{$db_name}}))) {
    my $status = $test_case_groups{$db_name}{$tc};
    
    next if ($status ne $hc_success && $type eq $hc_success); # HC success table
    next if ($status eq $hc_success && $type ne $hc_success); # HC failed table
      
    my $status_c = $status_colour{$status} ? $status_colour{$status} : $status_colour{'default'};
    my $tc_label = $tc;
       $tc_label =~ s/([A-Z])/ $1/g;
       $tc_label =~ s/My S Q L/MySQL/;
      
      
    my $hc_errors_count = '';
    if ($type ne $hc_success) {
      $hc_errors_count = ($test_cases{$db_name}{$tc} && scalar(@{$test_cases{$db_name}{$tc}})) ? scalar(@{$test_cases{$db_name}{$tc}}) : '-';
      $hc_errors_count = qq{
                  <td class="row_count" title="Number of errors reported">$hc_errors_count</td>};
    }
    $html .= sprintf( qq{              <tr>
                  <td><a href="#%s">%s</a></td>
                  <td style="vertical-align:middle"><span class="label %s">%s</td>%s
                </tr>\n},
             lc($tc), $tc_label, $status_c, $status, $hc_errors_count
    );
  }
  $html .=  qq{          </tbody>
          </table>
        </div>};

  return $html;
}

sub thousandify {
  my $number = shift;
  my $text = reverse $number;
  $text =~ s/(\d\d\d)(?=\d)(?!\d*\.)/$1,/gi;
  return scalar reverse $text;
}


sub get_html_head {
 return qq{
  <head>
    <title>HealthCheck results</title>
    <!-- CSS -->
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap-theme.min.css">
    <style type="text/css">
      .db_header { background-color:#EEE; padding:5px 6px; margin:5px 0px 10px; font-size:24px; }
      .table_hc_list { font-size:12px; margin-bottom:15px; max-width:375px; }
      .table_hc_list>thead>tr { background-color:#EEE; }
      .table_hc_list>thead>tr>th { padding:4px 5px; border-top:1px solid #DDD; }
      .table_hc_list>tbody>tr>td { padding:2px; }
      .legend_box { float:right; padding:5px; border:1px solid #CCC; }
      .bold_font { font-weight:bold; }
      .hc_header { padding:4px 6px; font-size:20px; margin:30px 0px 5px; background-color:#EEE; }
      .showhide_hc { border:none; background-color:transparent; }
      .showhide_hc:hover { color: #00F; }
      .hc_header_icon { padding:3px 6px; border:none; background-color:transparent; }
      .hc_status { margin-left:25px; vertical-align:middle; }
      .hc_top_link { float:right; }
      .hc_top_link a { text-decoration:none; padding:4px; }
      .hc_extra_row_div { padding:4px 0px; margin-left:30px; }
      .hc_font_1 { font-size:12px; }
      .pre_colour { color:#00D; }
      .new_colour { color:#ff8000; }
      .err_colour { color:#D00; }
      .black { color:#000; }
      .row_count { text-align:right; }
    </style>
    
    <!-- Javascript -->
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.12.2/jquery.min.js"></script>
    <script type="text/javascript">
      function showhide(id) {
        var div_id = "#"+id+"_$div_id_suffix";
        var button_id = "#"+id+"_$bt_id_suffix";
        
        if(\$(button_id).hasClass("glyphicon-menu-right")) {
          \$(button_id).removeClass("glyphicon-menu-right").addClass("glyphicon-menu-down");
          \$(div_id).show(150);
        }
        else {
          \$(button_id).removeClass("glyphicon-menu-down").addClass("glyphicon-menu-right");
          \$(div_id).hide(150);
        }
      }
      
      function showhide_row(id) {
        var div_id = "#"+id+"_$div_id_suffix";
        var button_id = "#"+id+"_$bt_id_suffix";
        
        if(\$(button_id).hasClass("glyphicon-plus-sign")) {
          \$(button_id).removeClass("glyphicon-plus-sign").addClass("glyphicon-minus-sign");
          \$(div_id).show(150);
        }
        else {
          \$(button_id).removeClass("glyphicon-minus-sign").addClass("glyphicon-plus-sign");
          \$(div_id).hide(150);
        }
      }
    </script>
  </head>
};
}

sub usage {
  my $msg = shift;
  print qq{
  $msg
  Usage: perl healthcheck_txt2html.pl [OPTIONS]
  
  Script to convert the healthcheck reports written in simple text file into an HTML file.
  
  Options:

    -help           Print this message
      
    -v              Ensembl release version, e.g. 88 (Required)
    -i              Path to the healthcheck reports text file (Required)
    -o              An HTML output file name (Required)
  } . "\n";
  exit(0);
}
